package com.speaknote.speaknote.controller;

import com.speaknote.speaknote.domain.AudioResult;
import com.speaknote.speaknote.dto.DialogueSummaryResult;
import com.speaknote.speaknote.dto.FileStorageResult;
import com.speaknote.speaknote.dto.HistoryResult;
import com.speaknote.speaknote.dto.TranscribeDiarizeResult;
import com.speaknote.speaknote.service.audioresult.AudioResultDbService;
import com.speaknote.speaknote.service.oai.DialogueSummaryService;
import com.speaknote.speaknote.service.storage.FileStorageService;

import com.speaknote.speaknote.service.oai.Gpt4oTranscribeDiarizeService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AudioController {

    private final FileStorageService fileStorageService;
    private final Gpt4oTranscribeDiarizeService  gpt4oTranscribeDiarizeService;
    private final DialogueSummaryService dialogueSummaryService;
    private final AudioResultDbService audioResultDbService;

    /**
     * 히스토리 조회 시 한 페이지당 반환할 데이터 개수.
     */
    private static final int PAGE_SIZE = 5;


    /**
     * 음성 파일을 업로드하고 다음 처리를 순차적으로 실행
     *
     * <p>처리 흐름:</p>
     * <ul>
     *   <li>STT 및 화자 분리</li>
     *   <li>대화 내용 정제 및 요약</li>
     *   <li>오디오 파일 저장</li>
     *   <li>DB 저장</li>
     * </ul>
     *
     * <p>비고:</p>
     * <ul>
     *   <li>DB 저장 실패 시 파일은 삭제처리</li>
     * </ul>
     *
     * @param recorderId 사용자 식별 쿠키
     * @param file 업로드된 오디오 파일 (wav)
     * @param session 업로드한 오디오 접근 권한 추가를 위한 HTTP 세션
     *
     * @return 처리 결과에 따른 http 응답, 성공 시 업로드된 데이터
     */
    @PostMapping("/process")
    public ResponseEntity<?> uploadAudio(@CookieValue(value = "recorderId") String recorderId,
                                         @RequestParam("file") MultipartFile file,
                                         HttpSession session) {

        // required = true 가 기본값이라 null 검증은 안해도 됨
        if(file.isEmpty()){

            log.warn("비정상요청 recorderId : {}, fileName : {}, contentType : {}, size : {}",
                    recorderId,
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getSize());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        // stt + 화자분리
        TranscribeDiarizeResult transcribeDiarizeResult = gpt4oTranscribeDiarizeService.transcribeDiarize(file);
        if(!transcribeDiarizeResult.isSuccess()){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "음성 인식 처리 실패"));
        }

        // 대화 정제 및 요약 생성
        DialogueSummaryResult dialogueSummaryResult = dialogueSummaryService.generateDialogueSummary(transcribeDiarizeResult.getDiarizedJson());
        if(!dialogueSummaryResult.isSuccess()){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "대화 요약 생성 실패"));
        }

        // wav 파일 저장
        FileStorageResult fileStorageResult = fileStorageService.upload(file, recorderId);
        if(!fileStorageResult.isSuccess()){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "오디오 파일 처리 실패"));
        }

        AudioResult audioResult = AudioResult.builder()
                .recorderId(recorderId)
                .dialogue(dialogueSummaryResult.getDialogue())
                .summary(dialogueSummaryResult.getSummary())
                .fileName(fileStorageResult.getFileName())
                .filePath(fileStorageResult.getFilePath())
                .fileSize(fileStorageResult.getFileSize())
                .build();

        // db 저장
        boolean isSuccess = audioResultDbService.save(audioResult); // 참조값 호출
        if(!isSuccess){
            log.warn("파일 저장은 성공했으나 db 저장 실패하여 파일 삭제 처리 : {}", audioResult.getFilePath());
            fileStorageService.deleteByPath(audioResult.getFilePath());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "데이터 저장 실패"));
        }

        HistoryResult historyResult = HistoryResult.builder()
                .idx(audioResult.getIdx())
                .dialogue(dialogueSummaryResult.getDialogue())
                .summary(dialogueSummaryResult.getSummary())
                .build();

        Map<Long, String> allowedAudio = (Map<Long, String>) session.getAttribute("allowedAudio");
        if(allowedAudio == null){
            allowedAudio = new HashMap<>();
        }
        allowedAudio.put(audioResult.getIdx(), audioResult.getFilePath());

        session.setAttribute("allowedAudio", allowedAudio);

        return ResponseEntity.status(HttpStatus.OK).body(Map.of("history", historyResult));
    }


    /**
     * 사용자의 음성 처리 히스토리를 커서 기반 페이징 방식으로 조회
     *
     * <p>페이징 방식:</p>
     * <ul>
     *   <li>lastIdx 보다 작은 데이터만 조회</li>
     *   <li>PAGE_SIZE + 1 만큼 조회하여 다음 페이지 존재 여부 판단</li>
     * </ul>
     *
     * <p>권한 처리:</p>
     * <ul>
     *   <li>조회된 오디오 정보는 세션에 저장</li>
     *   <li>오디오 스트리밍 요청 시 DB 재조회 없이 권한 검증</li>
     * </ul>
     *
     * @param recorderId 사용자 식별을 위한 쿠키 값
     * @param lastIdx    마지막으로 조회한 히스토리 idx, 최초 조회 시 가장 큰 unsigned integer값
     * @param session    오디오 접근 권한 관리를 위한 HTTP 세션
     *
     * @return 히스토리 목록 및 다음 페이지 존재 여부
     */
    @GetMapping("/history")
    public ResponseEntity<?> getHistory(@CookieValue(value = "recorderId") String recorderId,
                                        @RequestParam(defaultValue = "4294967295") long lastIdx,
                                        HttpSession session) {

        // no offset 커서 페이징 방식
        List<AudioResult> historyList = audioResultDbService.findHistory(recorderId, lastIdx, PAGE_SIZE+1);

        if(historyList == null){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "히스토리 조회 실패"));
        }

        boolean next = historyList.size() > PAGE_SIZE;
        if(next){
            // 페이지 크기 +1 값은 다음 페이지 유무를 판별하고 버림
            historyList.remove(PAGE_SIZE);
        }

        // audio url api 요청 때 db 두 번 타지 않도록 세션에 조회 정보 추가
        Map<Long, String> allowedAudio = (Map<Long, String>) session.getAttribute("allowedAudio");
        if(allowedAudio == null){
            allowedAudio = new HashMap<>();
        }

        List<HistoryResult> historyResultList = new ArrayList<>();
        for(AudioResult history : historyList){
            allowedAudio.put(history.getIdx(), history.getFilePath());
            historyResultList.add(
                    HistoryResult.builder()
                            .idx(history.getIdx())
                            .dialogue(history.getDialogue())
                            .summary(history.getSummary())
                            .created(history.getCreated())
                            .build()
            );
        }

        session.setAttribute("allowedAudio",  allowedAudio);

        return ResponseEntity.status(HttpStatus.OK).body(Map.of("next", next, "history", historyResultList));
    }


    /**
     * 특정 히스토리 idx 에 해당하는 오디오 파일을 스트리밍 방식으로 반환
     *
     * <p>접근 제어:</p>
     * <ul>
     *   <li>세션에 저장된 접근 허용 목록에 포함된 경우만 접근 가능</li>
     * </ul>
     *
     * <p>스트리밍:</p>
     * <ul>
     *   <li>HTTP Range 요청 지원</li>
     *   <li>audio/wav 형식으로 Partial Content(206) 반환</li>
     * </ul>
     *
     * @param idx     오디오 히스토리 식별자
     * @param headers HTTP Range 헤더
     * @param session 오디오 접근 권한 확인을 위한 HTTP 세션
     *
     * @return 오디오 ResourceRegion 또는 오류 응답
     */
    @GetMapping("/audio/{idx}") // 제네릭 타입 명시 안하면 컨버터 에러
    public ResponseEntity<ResourceRegion> getAudio(@PathVariable Long idx,
                                      @RequestHeader HttpHeaders headers,
                                      HttpSession session) {

        Map<Long, String> allowedAudio = (Map<Long, String>) session.getAttribute("allowedAudio");
        if(allowedAudio == null || !allowedAudio.containsKey(idx)){ // containsKey << 키 존재유무 확인
            log.warn("권한 없는 파일 접근 시도 : idx= {}", idx);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } else if (allowedAudio.get(idx) == null) {
            log.warn("파일 경로가 존재하지 않음 : idx= {}", idx);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        try {
            String filePath = allowedAudio.get(idx);
            Path path = Paths.get(filePath);
            Resource resource = new FileSystemResource(path);

            if(!resource.exists()){
                log.warn("오디오 파일 존재하지 않음 : idx= {}", idx);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            long contentLength = resource.contentLength();
            HttpRange range = headers.getRange().isEmpty() ? HttpRange.createByteRange(0) : headers.getRange().get(0);

            long start = range.getRangeStart(contentLength);
            long end = range.getRangeEnd(contentLength);
            long rangeLength = Math.min(1024 * 1024, end - start + 1);

            ResourceRegion region = new ResourceRegion(resource, start, rangeLength);

            log.info("오디오 반환 성공");
            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .contentType(MediaType.parseMediaType("audio/wav"))
                    .contentLength(region.getCount())
                    .body(region);

        } catch (IOException e) {
            log.error("오디오 파일 불러오는 중 오류 발생 : idx= {}", idx, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }


}

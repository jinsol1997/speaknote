package com.speaknote.speaknote.controller;

import com.speaknote.speaknote.domain.AudioResult;
import com.speaknote.speaknote.dto.DialogueSummaryResult;
import com.speaknote.speaknote.dto.FileStorageResult;
import com.speaknote.speaknote.dto.TranscribeDiarizeResult;
import com.speaknote.speaknote.service.audioresult.AudioResultDbService;
import com.speaknote.speaknote.service.oai.DialogueSummaryService;
import com.speaknote.speaknote.service.storage.FileStorageService;

import com.speaknote.speaknote.service.oai.Gpt4oTranscribeDiarizeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@Slf4j
@RestController
@RequestMapping("/api/audio")
@RequiredArgsConstructor
public class AudioController {

    private final FileStorageService fileStorageService;
    private final Gpt4oTranscribeDiarizeService  gpt4oTranscribeDiarizeService;
    private final DialogueSummaryService dialogueSummaryService;
    private final AudioResultDbService audioResultDbService;

    @PostMapping("/process")
    public ResponseEntity<?> uploadAudio(@CookieValue(value = "recorderId") String recorderId,
                                         @RequestParam("file") MultipartFile file) {

        // required = true 가 기본값이라 null 검증은 안해도 됨
        if(file.isEmpty()){

            log.warn("비정상요청 recorderId : {}, fileName : {}, contentType : {}, size : {}",
                    recorderId,
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getSize());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        TranscribeDiarizeResult transcribeDiarizeResult = gpt4oTranscribeDiarizeService.transcribeDiarize(file);
        if(!transcribeDiarizeResult.isSuccess()){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "음성 인식 처리 실패"));
        }

        DialogueSummaryResult dialogueSummaryResult = dialogueSummaryService.generateDialogueSummary(transcribeDiarizeResult.getDiarizedJson());
        if(!dialogueSummaryResult.isSuccess()){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "대화 요약 생성 실패"));
        }

        FileStorageResult fileStorageResult = fileStorageService.upload(file, recorderId);
        if(!fileStorageResult.isSuccess()){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "녹음파일 처리 실패"));
        }

        AudioResult audioResult = AudioResult.builder()
                .recorderId(recorderId)
                .dialogue(dialogueSummaryResult.getDialogue())
                .summary(dialogueSummaryResult.getSummary())
                .fileName(fileStorageResult.getFileName())
                .filePath(fileStorageResult.getFilePath())
                .fileSize(fileStorageResult.getFileSize())
                .build();

        boolean isSuccess = audioResultDbService.save(audioResult);
        if(!isSuccess){
            log.warn("파일 저장은 성공했으나 db 저장 실패하여 파일 삭제 처리 : {}", audioResult.getFilePath());
            fileStorageService.deleteByPath(audioResult.getFilePath());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "데이터 저장 실패"));
        }
        
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("dialogue", dialogueSummaryResult.getDialogue());
        resultMap.put("summary", dialogueSummaryResult.getSummary());

        return ResponseEntity.status(HttpStatus.OK).body(resultMap);
    }

}

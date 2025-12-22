package com.speaknote.speaknote.service.storage;

import com.speaknote.speaknote.domain.OldData;
import com.speaknote.speaknote.dto.FileStorageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


/**
 * 로컬 파일 시스템에 음성 파일을 저장/삭제하는 FileStorageService 구현체
 *
 * <p>역할:</p>
 * <ul>
 *   <li>지정된 업로드 경로에 음성 파일을 저장</li>
 *   <li>저장된 파일을 경로 기준으로 삭제</li>
 * </ul>
 *
 * <p>설계 정책:</p>
 * <ul>
 *   <li>업로드 디렉토리는 서비스 시작 시 1회 초기화</li>
 *   <li>저장 실패는 예외 전파 대신 FileStorageResult.success=false 로 표현</li>
 *   <li>삭제는 파일이 없어도 DB 정리 가능하도록 true 반환</li>
 * </ul>
 *
 * <p>주의:</p>
 * <ul>
 *   <li>업로드 디렉토리 생성/접근이 불가능한 경우 서비스 자체가 불가능하므로 RuntimeException을 발생시킴</li>
 *   <li>upload()는 MultipartFile을 실제 파일로 이동시키므로, 호출 이후 동일 MultipartFile을 사용하지 못함</li>
 * </ul>
 */
@Slf4j
@Service
public class LocalFileStorageService implements FileStorageService {

    /**
     * 업로드 파일이 저장될 루트 디렉터리 경로.
     *
     * <p>application property(file.upload-dir) 기반으로 초기화되며,
     * 절대 경로로 정규화(normalize)</p>
     */
    private final Path uploadPath;

    public LocalFileStorageService(@Value("${file.upload-dir}") String uploadDir) {

        // 파일 경로 최초 실행시에만 설정하도록 수정
        this.uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        try{
            if (!Files.exists(this.uploadPath)) {
                log.info("upload 폴더 생성");
                Files.createDirectories(uploadPath);
            }
        } catch (IOException e){
            log.error("upload 폴더 경로 설정 중 오류 발생 경로 : {}", uploadPath, e);
            throw new RuntimeException(e);  // 핸들링 하지 않고 실행단계에서 터지도록 checked -> unchecked로 다시 던짐
        }

    }


    /**
     * 음성 파일을 로컬 디렉터리에 저장
     *
     * <p>파일명 규칙:</p>
     * <ul>
     *   <li>{yyyyMMdd_HHmmss}_{recorderId}.wav</li>
     * </ul>
     *
     * <p>동작:</p>
     * <ul>
     *   <li>MultipartFile을 로컬 파일로 저장(transferTo)</li>
     *   <li>파일명, 저장 경로, 사이즈 및 결과를 반환</li>
     * </ul>
     *
     * <p>반환/실패 정책:</p>
     * <ul>
     *   <li>성공 시: success=true 및 fileName/filePath/fileSize 포함</li>
     *   <li>실패 시: success=false 반환</li>
     * </ul>
     *
     * @param file       업로드된 음성 파일
     * @param recorderId 파일 소유자 식별자
     * @return 파일 저장 결과
     */
    @Override
    public FileStorageResult upload(MultipartFile file, String recorderId) {

        try {

            // 파일명: 시간_쿠키값.wav
            String timePrefix = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

            String saveFilename = timePrefix + "_" + recorderId + ".wav";

            Path target = uploadPath.resolve(saveFilename);

            // 메모리 상의 multipartFile을 파일로 저장 후 메모리 삭제하므로 서비스 중 마지막에 실행돼야함
            file.transferTo(target.toFile());

            log.info("파일 저장 성공 : {}", target);
            // 성공 결과 반환
            return FileStorageResult.builder()
                    .success(true)
                    .fileName(saveFilename)
                    .filePath(target.toString())
                    .fileSize(file.getSize())
                    .build();

        } catch (IOException e) {

            log.error("파일 저장 중 오류 발생", e);

            return FileStorageResult.builder()
                    .success(false)
                    .build();
        }

    }


    /**
     * 지정된 경로의 파일을 삭제
     *
     * <p>설계 정책:</p>
     * <ul>
     *   <li>filePath가 비어있거나 파일이 존재하지 않아도 true를 반환</li>
     *   <li>이는 스케쥴러에서 파일이 없어도 DB는 정리 가능하게 하기 위함</li>
     * </ul>
     *
     * <p>반환/실패 정책:</p>
     * <ul>
     *   <li>삭제 성공 시 true 반환</li>
     *   <li>삭제 대상이 없거나(경로 없음/파일 미존재) 정리 불필요하다고 판단한 경우 true 반환</li>
     *   <li>삭제 시도 중 예외 발생 시 false 반환</li>
     * </ul>
     *
     * @param filePath 삭제할 파일의 전체 경로
     * @return 삭제 성공 여부
     */
    @Override
    public boolean deleteByPath(String filePath) {

        try {

            if (filePath == null || filePath.isBlank()){
                log.warn("삭제 대상 파일 경로 정보 없음");
                return true;    // db상에서 삭제되도록 true 반환
            }

            Path path = Paths.get(filePath);

            if (!Files.exists(path)){
                log.warn("존재하지 않는 파일 삭제 요청 : {}", filePath);
                return true;
            }

            Files.delete(path);
            log.info("파일 삭제 성공 : {}", filePath);
            return true;

        } catch (Exception e) {
            log.error("파일 삭제 중 오류 발생 : {}", filePath, e);
            return false;
        }

    }

}

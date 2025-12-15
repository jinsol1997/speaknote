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

@Slf4j
@Service
public class LocalFileStorageService implements FileStorageService {

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
            throw new RuntimeException(e);
        }

    }

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

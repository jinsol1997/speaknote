package com.speaknote.speaknote.service.storage;

import com.speaknote.speaknote.dto.FileStorageResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class LocalFileStorageService implements FileStorageService {

    private final Path uploadPath;

    public LocalFileStorageService(@Value("${file.upload-dir}") String uploadDir) {

        // 파일 경로 최초 실행시에만 설정하도록 수정
        this.uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        try{
            if (!Files.exists(this.uploadPath)) {
                Files.createDirectories(uploadPath);
            }
        } catch (IOException e){
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

            // 성공 결과 반환
            return FileStorageResult.builder()
                    .success(true)
                    .fileName(saveFilename)
                    .filePath(target.toString())
                    .fileSize(file.getSize())
                    .build();

        } catch (IOException e) {
            return FileStorageResult.builder()
                    .success(false)
                    .errorMessage("파일 저장 중 오류 발생")
                    .build();
        }


    }
}

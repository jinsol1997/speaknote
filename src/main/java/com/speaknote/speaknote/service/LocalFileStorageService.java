package com.speaknote.speaknote.service;

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

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public FileStorageResult upload(MultipartFile file, String recorder_id) {

        try {
            // 업로드 디렉토리 생성
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 파일명: 시간_쿠키값.wav
            String timePrefix = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

            String saveFilename = timePrefix + "_" + recorder_id + ".wav";

            Path target = uploadPath.resolve(saveFilename);

            // 실제 저장
            file.transferTo(target.toFile());

            // 성공 결과 반환
            return FileStorageResult.builder()
                    .success(true)
                    .message("파일 업로드 완료")
                    .fileName(saveFilename)
                    .filePath(target.toString())
                    .fileSize(file.getSize())
                    .build();

        } catch (IOException e) {
            return FileStorageResult.builder()
                    .success(false)
                    .message("파일 저장 중 오류 발생")
                    .build();
        }


    }
}

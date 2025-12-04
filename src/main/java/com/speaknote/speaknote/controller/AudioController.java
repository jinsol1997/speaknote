package com.speaknote.speaknote.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/audio")
public class AudioController {

    @PostMapping("/process")
    public ResponseEntity<?> uploadAudio(HttpServletRequest request, @RequestParam("file") MultipartFile file) {

        Map<String, Object> result = new HashMap<>();

        try {
            if(file.isEmpty()) {
                result.put("text", "업로드된 파일 없음");
                return ResponseEntity.badRequest().body(result);
            }

            Path uploadDir = Paths.get("uploads").toAbsolutePath().normalize();
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            String timePrefix = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

            Cookie[] cookies = request.getCookies();
            String record_id = null;
            if (cookies != null) {
                for (Cookie c : cookies) {
                    if ("recorder_id".equals(c.getName())) {
                        record_id = c.getValue();
                        break; // 찾았으면 바로 종료
                    }
                }
            }

            if (record_id == null) {
                result.put("text", "쿠키 없음");
                return ResponseEntity.badRequest().body(result);
            }

            String saveFilename = timePrefix + "_" + record_id + ".wav";

            Path target = uploadDir.resolve(saveFilename);

            file.transferTo(target.toFile());

            result.put("text", "파일 업로드 완료");
            result.put("savedFilename", saveFilename);
            result.put("savedPath", target.toString());
            result.put("size", file.getSize());

            return ResponseEntity.ok(result);

        } catch (IOException e){
            e.printStackTrace();
            result.put("text", "파일 저장 중 오류 발생");
            result.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }

    }

}

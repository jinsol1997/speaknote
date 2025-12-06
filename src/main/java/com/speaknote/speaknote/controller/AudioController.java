package com.speaknote.speaknote.controller;

import com.speaknote.speaknote.dto.FileStorageResult;
import com.speaknote.speaknote.service.FileStorageService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/api/audio")
@RequiredArgsConstructor
public class AudioController {

    private final FileStorageService fileStorageService;

    @PostMapping("/process")
    public ResponseEntity<?> uploadAudio(@CookieValue(value = "recorder_id") String recorder_id,
                                         @RequestParam("file") MultipartFile file) {

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(
                            FileStorageResult.builder()
                                    .success(false)
                                    .message("업로드된 파일 없음")
                                    .build()
                    );
        }

        FileStorageResult fileStorageResult = fileStorageService.upload(file, recorder_id);
        HttpStatus status = fileStorageResult.isSuccess() ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR;

        return ResponseEntity.status(status).body(fileStorageResult);
    }

}

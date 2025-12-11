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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;


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

        if(file == null || file.isEmpty() || recorderId == null || recorderId.isBlank()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        TranscribeDiarizeResult transcribeDiarizeResult = gpt4oTranscribeDiarizeService.transcribeDiarize(file);
        if(!transcribeDiarizeResult.isSuccess()){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        DialogueSummaryResult dialogueSummaryResult = dialogueSummaryService.generateDialogueSummary(transcribeDiarizeResult.getDiarizedJson());
        if(!dialogueSummaryResult.isSuccess()){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        FileStorageResult fileStorageResult = fileStorageService.upload(file, recorderId);
        if(!fileStorageResult.isSuccess()){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("dialogue", dialogueSummaryResult.getDialogue());
        resultMap.put("summary", dialogueSummaryResult.getSummary());

        return ResponseEntity.status(HttpStatus.OK).body(resultMap);
    }

}

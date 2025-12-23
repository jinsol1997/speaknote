package com.speaknote.speaknote;

import com.speaknote.speaknote.controller.AudioController;
import com.speaknote.speaknote.domain.AudioResult;
import com.speaknote.speaknote.service.audioresult.AudioResultDbService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@SpringBootTest
public class AudioControllerTest {

    @Autowired
    private AudioResultDbService  audioResultDbService;

    @Autowired
    private AudioController  audioController;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Test
    public void historyServiceTest(){

        List<AudioResult> historyList = audioResultDbService.findHistory("41450a7e-9b07-4699-a648-135f1e0acb42", 4294967295L, 5);

        for(AudioResult audioResult : historyList){
            System.out.println(audioResult.getIdx());
            System.out.println(audioResult.getDialogue());
            System.out.println(audioResult.getSummary());
            System.out.println(audioResult.getFilePath());
            System.out.println(audioResult.getCreated());
        }

    }

    @Test
    public void historyControllerTest(){

        ResponseEntity<?> responseEntity = audioController.getHistory("41450a7e-9b07-4699-a648-135f1e0acb42", 4294967295L, null);
        System.out.println(responseEntity.getBody());

    }

    @Test
    public void uploadAudioControllerTest() throws IOException {

        Path path = Paths.get(uploadDir, "test4.wav").toAbsolutePath().normalize();
        byte[] bytes = Files.readAllBytes(path);

        MultipartFile multipartFile = new MockMultipartFile("file", "test.wav", "audio/wav",  bytes);

        MockHttpSession session = new MockHttpSession();
        ResponseEntity<?> responseEntity = audioController.uploadAudio("41450a7e-9b07-4699-a648-135f1e0acb42", multipartFile, session);

        System.out.println(responseEntity.getBody());
    }
}

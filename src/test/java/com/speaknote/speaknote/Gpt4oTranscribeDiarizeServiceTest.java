package com.speaknote.speaknote;

import com.speaknote.speaknote.dto.TranscribeDiarizeResult;
import com.speaknote.speaknote.service.Gpt4oTranscribeDiarizeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootTest
public class Gpt4oTranscribeDiarizeServiceTest {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Autowired
    Gpt4oTranscribeDiarizeService  gpt4oTranscribeDiarizeService;

    @Test
    public void test() throws IOException {

        Path path = Paths.get(uploadDir, "test.wav").toAbsolutePath().normalize();
        byte[] bytes = Files.readAllBytes(path);

        MultipartFile multipartFile = new MockMultipartFile("file", "test.wav", "audio/wav",  bytes);

        TranscribeDiarizeResult transcribeDiarizeResult = gpt4oTranscribeDiarizeService.transcribeDiarize(multipartFile);

        System.out.println(transcribeDiarizeResult.isSuccess());
        System.out.println(transcribeDiarizeResult.getDiarizedJson());

    }

}

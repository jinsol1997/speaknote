package com.speaknote.speaknote;

import com.speaknote.speaknote.domain.AudioResult;
import com.speaknote.speaknote.service.audioresult.AudioResultDbService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class AudioResultDbServiceTest {

    @Autowired
    AudioResultDbService audioResultDbService;

    @Test
    public void test(){

        AudioResult audioResult = AudioResult.builder()
                .recorderId("testId2")
                .dialogue("dialogue")
                .summary("summary")
                .fileName("fileName")
                .filePath("filePath")
                .fileSize(100)
                .build();

        audioResultDbService.save(audioResult);
    }
}

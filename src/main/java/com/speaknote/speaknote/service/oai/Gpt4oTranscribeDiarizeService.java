package com.speaknote.speaknote.service.oai;

import com.speaknote.speaknote.dto.TranscribeDiarizeResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;

@Slf4j
@Service
public class Gpt4oTranscribeDiarizeService extends AbstractOpenAiService {

    public Gpt4oTranscribeDiarizeService(@Value("${openai.api-key}") String apiKey) {
        super(apiKey);
    }

    public TranscribeDiarizeResult transcribeDiarize(MultipartFile file) {

        try {
            // multipart/form-data
            MultipartBodyBuilder body = new  MultipartBodyBuilder();
            body.part("model", "gpt-4o-transcribe-diarize");
            body.part("response_format", "diarized_json");

            body.part("file", file.getResource())
                    .filename("audio.wav")
                    .contentType(MediaType.parseMediaType("audio/wav"));

            body.part("chunking_strategy", "auto");
            body.part("language", "ko");

            String diarizedJson = webClient.post()
                    .uri("/audio/transcriptions")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(body.build()))
                    .retrieve() // 응답 200 or 4~500 구분해서 WebClientResponseException 던지고 응답 body를 넘김
                    .bodyToMono(String.class)   // 응답 올 바디를 string 형태로 받음
                    .timeout(Duration.ofSeconds(60))
                    .block();   // 동기식으로 처리

            log.info("gpt-4o-transcribe-diarize 모델 api 호출 성공");

            return TranscribeDiarizeResult.builder()
                    .success(true)
                    .diarizedJson(diarizedJson)
                    .build();

        }
        catch (WebClientResponseException e) {
            // api 요청 에러
//            System.out.println("=== OpenAI ERROR BODY ===");
//            System.out.println(e.getResponseBodyAsString());
//            System.out.println("==========================");
            
            log.error("gpt-4o-transcribe-diarize 모델 api 요청 에러 : {}", e.getResponseBodyAsString());

            return TranscribeDiarizeResult.builder()
                    .success(false)
                    .build();

        }
        catch (Exception e) {

            log.error("gpt-4o-transcribe-diarize 모델 api 호출 중 오류 발생", e);

            return TranscribeDiarizeResult.builder()
                    .success(false)
                    .build();

        }


    }

}

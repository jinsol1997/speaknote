package com.speaknote.speaknote.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
public class Gpt4oTranscribeDiarizeService {

    private final WebClient webClient;

    public Gpt4oTranscribeDiarizeService(@Value("${openai.api-key}") String apiKey) {

        this.webClient = WebClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .build();
    }

    public String transcribeDiarize(MultipartFile file) {

        try {
            MultipartBodyBuilder body = new  MultipartBodyBuilder();
             body.part("model", "gpt-4o-transcribe-diarize");
             body.part("response_format", "diarized_json");

//            body.part("model", "whisper-1");
//            body.part("response_format", "json");
            // body.part("language", "ko");

            // body.build() 미리 해두려면 MultiValueMap 선언해서 담아둬야함

//            body.part("file", new ByteArrayResource(file.getBytes()){
//                // 상위 추상클래스에서 구현된 getFilename이 null을 반환해서 오버라이드 해야함
//                @Override
//                public String getFilename() {
//                    return "audio.wav";
//                }
//            });

            body.part("file", file.getResource())
                    .filename("audio")
                    .contentType(MediaType.parseMediaType("audio/wav"));


            return webClient.post()
                    .uri("/audio/transcriptions")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(body.build()))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

        }
        catch (WebClientResponseException e) {
            // api 요청 에러
            System.out.println("=== OpenAI ERROR BODY ===");
            System.out.println(e.getResponseBodyAsString());
            System.out.println("==========================");

            throw new RuntimeException("gpt-4o-transcribe-diarize 모델 api 호출 실패", e);
        }
        catch (Exception e) {
            throw new RuntimeException("gpt-4o-transcribe-diarize 모델 api 호출 실패", e);
        }


    }

}

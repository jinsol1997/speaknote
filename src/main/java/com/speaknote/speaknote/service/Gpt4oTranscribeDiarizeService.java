package com.speaknote.speaknote.service;

import com.speaknote.speaknote.dto.TranscribeDiarizeResult;
import org.springframework.beans.factory.annotation.Value;
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

    public TranscribeDiarizeResult transcribeDiarize(MultipartFile file) {

        try {
            MultipartBodyBuilder body = new  MultipartBodyBuilder();
            body.part("model", "gpt-4o-transcribe-diarize");
            body.part("response_format", "diarized_json");

            body.part("file", file.getResource())
                    .filename("audio")
                    .contentType(MediaType.parseMediaType("audio/wav"));

            body.part("chunking_strategy", "auto");
            body.part("language", "ko");

            String message = webClient.post()
                    .uri("/audio/transcriptions")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(body.build()))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return TranscribeDiarizeResult.builder()
                    .success(true)
                    .message(message)
                    .build();

        }
        catch (WebClientResponseException e) {
            // api 요청 에러
            System.out.println("=== OpenAI ERROR BODY ===");
            System.out.println(e.getResponseBodyAsString());
            System.out.println("==========================");

            return TranscribeDiarizeResult.builder()
                    .success(false)
                    .message(e.getResponseBodyAsString())
                    .build();

        }
        catch (Exception e) {

            return TranscribeDiarizeResult.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build();

        }


    }

}

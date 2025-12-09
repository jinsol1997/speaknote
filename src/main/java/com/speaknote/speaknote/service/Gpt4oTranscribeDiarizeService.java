package com.speaknote.speaknote.service;

import com.speaknote.speaknote.dto.TranscribeDiarizeResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
public class Gpt4oTranscribeDiarizeService extends AbstractOpenAiService {

    public Gpt4oTranscribeDiarizeService(@Value("${openai.api-key}") String apiKey) {
        super(apiKey);
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

            String diarizedJson = webClient.post()
                    .uri("/audio/transcriptions")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(body.build()))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return TranscribeDiarizeResult.builder()
                    .success(true)
                    .diarizedJson(diarizedJson)
                    .build();

        }
        catch (WebClientResponseException e) {
            // api 요청 에러
            System.out.println("=== OpenAI ERROR BODY ===");
            System.out.println(e.getResponseBodyAsString());
            System.out.println("==========================");

            return TranscribeDiarizeResult.builder()
                    .success(false)
                    .errorMessage(e.getResponseBodyAsString())
                    .build();

        }
        catch (Exception e) {

            return TranscribeDiarizeResult.builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .build();

        }


    }

}

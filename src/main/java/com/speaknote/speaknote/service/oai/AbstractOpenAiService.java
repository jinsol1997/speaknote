package com.speaknote.speaknote.service.oai;

import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;

public abstract class AbstractOpenAiService {

    protected final WebClient webClient;

    public AbstractOpenAiService(String apiKey) {

        this.webClient = WebClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .build();
    }

}

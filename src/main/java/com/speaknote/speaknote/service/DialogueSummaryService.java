package com.speaknote.speaknote.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
public class DialogueSummaryService extends AbstractOpenAiService{

    private final ObjectMapper objectMapper;

    public DialogueSummaryService(@Value("${openai.api-key}") String apiKey) {
        super(apiKey);
        // JSON에 DTO에 없는 데이터 와도 오류X
        this.objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public void gererateDialogueSummary(String diarizedJson){

        try {

        } catch (WebClientResponseException e) {

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

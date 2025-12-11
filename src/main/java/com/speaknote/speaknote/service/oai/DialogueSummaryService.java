package com.speaknote.speaknote.service.oai;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.speaknote.speaknote.dto.DialogueSummaryResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DialogueSummaryService extends AbstractOpenAiService {

    private final ObjectMapper objectMapper;

    public DialogueSummaryService(@Value("${openai.api-key}") String apiKey) {
        super(apiKey);
        // JSON에 DTO에 없는 데이터 와도 오류X
        this.objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public DialogueSummaryResult generateDialogueSummary(String diarizedJson){

        try {

            String systemContent = """
                    너는 한국어를 사용하는 대화 요약/정제 도우미다.
                    - 입력은 gpt-4o-transcribe-diarize가 반환한 diarized_json 문자열이다.
                    - 사용자의 실제 발화를 최대한 보존하되, 오타/오역/이상한 띄어쓰기를 자연스럽게 정리한다.
                    - 게임/IT 용어는 그대로 둔다.
                    - 반드시 JSON만 반환 :
                        {
                        "dialogue": "정제된 최종 대화록 문자열",
                        "summary": "대화의 핵심을 요약한 한국어 문단"
                        }
                    - JSON 외의 텍스트는 절대 붙이지 마라.
                    """;

            String userContent = """
                    아래는 diarized_json이다.
                    내용을 기반으로:
                    1) 사람이 읽기 좋게 정리한 "dialogue"
                    2) 핵심 요약 "summary"
                    
                    반드시 아래 형식만 출력:
                    {
                      "dialogue": "...",
                      "summary": "..."
                    }
                    
                    === 시작 ===
                    %s
                    === 끝 ===
                    
                    """.formatted(diarizedJson);

            // 요청 데이터 형태
//            {
//                "model": "gpt-5.1",
//
//                    "response_format": {
//                "type": "json_schema",          // json 형식 강제
//                        "json_schema":
//                    "name": "DialogueFormat",
//                    "strict": true,               // schema 위반 불가
//                            "schema": {
//                        "type": "object",
//                                "properties": {
//                            "dialogue": { "type": "string" },     // dialogue 문자열
//                            "summary":  { "type": "string" }      // summary 문자열
//                        },
//                        "required": ["dialogue", "summary"],    // 두 필드는 필수
//                        "additionalProperties": false           // 필수 필드 외 필드 생성 금지
//                    }
//                }
//            },
//
//                "messages": [
//                {
//                    "role": "system",
//                        "content": "… 시스템 프롬프트 내용 …"
//                },
//                {
//                    "role": "user",
//                        "content": "… diarized_json 입력 …"
//                }
//  ]
//            }

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-5.1");

            Map<String, Object> schema = new HashMap<>();
            schema.put("type", "object");

            Map<String, Object> properties = new HashMap<>();
            properties.put("dialogue", Map.of("type", "string"));
            properties.put("summary", Map.of("type", "string"));
            schema.put("properties", properties);

            schema.put("required", List.of("dialogue", "summary"));
            schema.put("additionalProperties", false);

            Map<String, Object> jsonSchema = new HashMap<>();
            jsonSchema.put("name", "DialogueFormat");
            jsonSchema.put("strict", true);
            jsonSchema.put("schema", schema);

            Map<String, Object> responseFormat = new HashMap<>();
            responseFormat.put("type", "json_schema");
            responseFormat.put("json_schema", jsonSchema);

            requestBody.put("response_format", responseFormat);

            List<Map<String, String>> messages = new ArrayList<>();

            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", systemContent);

            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", userContent);

            messages.add(systemMessage);
            messages.add(userMessage);

            requestBody.put("messages", messages);

            String response = webClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            String content = objectMapper.readTree(response).at("/choices/0/message/content").asText();
            Map<String, Object> map =  objectMapper.readValue(content, Map.class);
            String dialogue = (String) map.get("dialogue");
            String summary = (String) map.get("summary");

            return DialogueSummaryResult.builder()
                    .success(true)
                    .dialogue(dialogue)
                    .summary(summary)
                    .build();

        } catch (WebClientResponseException e) {

            System.out.println(e.getResponseBodyAsString());

            return DialogueSummaryResult.builder()
                    .success(false)
                    .errorMessage(e.getResponseBodyAsString())
                    .build();

        } catch (Exception e) {

            System.out.println(e.getMessage());

            return DialogueSummaryResult.builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .build();

        }

    }

}

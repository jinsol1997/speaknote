package com.speaknote.speaknote.service.oai;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.speaknote.speaknote.dto.DialogueSummaryResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * diarized_json(화자 분리된 STT 결과)을 입력받아
 * 사람이 읽기 좋은 대화록(dialogue)과 요약(summary)을 생성하는 서비스
 *
 * <p>주요 역할:</p>
 * <ul>
 *   <li>OpenAI Chat Completions API 호출</li>
 *   <li>입력 diarized_json 기반으로 대화 텍스트 정제</li>
 *   <li>대화 핵심 요약 생성</li>
 *   <li>JSON 스키마(response_format)를 통해 응답 형식을 강제</li>
 * </ul>
 *
 * <p>반환 정책:</p>
 * <ul>
 *   <li>성공 시: DialogueSummaryResult.success=true 및 dialogue/summary 포함</li>
 *   <li>실패 시: DialogueSummaryResult.success=false 반환 (예외는 내부에서 처리)</li>
 * </ul>
 */
@Slf4j
@Service
public class DialogueSummaryService extends AbstractOpenAiService {

    private final ObjectMapper objectMapper;

    public DialogueSummaryService(@Value("${openai.api-key}") String apiKey) {
        super(apiKey);
        // JSON에 DTO에 없는 데이터 와도 오류X
        this.objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }


    /**
     * diarized_json 문자열을 기반으로 정제된 대화록(dialogue)과 요약(summary)을 생성
     *
     * <p>요청 특징:</p>
     * <ul>
     *   <li>Chat Completions API (/chat/completions) 호출</li>
     *   <li>response_format=json_schema 를 사용하여 JSON 형태 응답을 강제</li>
     *   <li>모델 응답은 choices[0].message.content 에 JSON 문자열로 포함됨</li>
     * </ul>
     *
     * <p>타임아웃:</p>
     * <ul>
     *   <li>최대 300초(timeout) 내 응답이 없으면 예외 처리</li>
     * </ul>
     *
     * <p>실패 처리:</p>
     * <ul>
     *   <li>WebClientResponseException 발생 시 success=false 반환</li>
     *   <li>기타 예외 발생 시 success=false 반환</li>
     *   <li>예외는 상위로 전파하지 않음</li>
     * </ul>
     *
     * @param diarizedJson gpt-4o-transcribe-diarize 결과인 diarized_json 문자열
     * @return 대화 정제 및 요약 결과
     */
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
            requestBody.put("model", "gpt-5.2");

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
                    .timeout(Duration.ofSeconds(300))
                    .block();

            String content = objectMapper.readTree(response).at("/choices/0/message/content").asText();
            Map<String, Object> map =  objectMapper.readValue(content, Map.class);
            String dialogue = (String) map.get("dialogue");
            String summary = (String) map.get("summary");
            
            log.info("gpt-5.2 모델 api 호출 성공");

            return DialogueSummaryResult.builder()
                    .success(true)
                    .dialogue(dialogue)
                    .summary(summary)
                    .build();

        } catch (WebClientResponseException e) {

            log.error("gpt-5.2 모델 api 요청 에러 : {}", e.getResponseBodyAsString());

            return DialogueSummaryResult.builder()
                    .success(false)
                    .build();

        } catch (Exception e) {

            log.error("gpt-5.2 모델 api 호출 중 오류 발생", e);

            return DialogueSummaryResult.builder()
                    .success(false)
                    .build();

        }

    }

}

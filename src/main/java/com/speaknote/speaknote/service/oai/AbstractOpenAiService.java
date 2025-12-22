package com.speaknote.speaknote.service.oai;

import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;


/**
 * OpenAI API 호출을 위한 공통 WebClient 설정을 위한 추상 클래스
 *
 * <p>역할:</p>
 * <ul>
 *   <li>OpenAI API base URL 설정</li>
 *   <li>Authorization 헤더 공통 구성</li>
 * </ul>
 *
 * <p>설계 의도:</p>
 * <ul>
 *   <li>OpenAI API 호출 설정을 한 곳에서 관리</li>
 *   <li>각 기능별 서비스는 이 클래스를 상속받아 사용</li>
 * </ul>
 */
public abstract class AbstractOpenAiService {

    protected final WebClient webClient;

    public AbstractOpenAiService(String apiKey) {

        this.webClient = WebClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .build();
    }

}

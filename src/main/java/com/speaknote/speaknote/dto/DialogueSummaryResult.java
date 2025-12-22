package com.speaknote.speaknote.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;


/**
 * 대화 정제 및 요약 결과를 나타내는 DTO
 *
 * <p>용도:</p>
 * <ul>
 *   <li>OpenAI 기반 대화 요약 처리 결과 전달</li>
 *   <li>성공/실패 여부와 결과 데이터</li>
 * </ul>
 *
 * <p>주의:</p>
 * <ul>
 *   <li>success=false 인 경우 dialogue, summary 값은 의미를 갖지 않음</li>
 * </ul>
 */
@AllArgsConstructor
@Getter
@Builder
public class DialogueSummaryResult {
    private boolean success;
    private String dialogue;
    private String summary;
}

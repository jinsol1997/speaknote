package com.speaknote.speaknote.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;


/**
 * 음성 STT 및 화자 분리 처리 결과를 나타내는 DTO
 *
 * <p>용도:</p>
 * <ul>
 *   <li>gpt-4o-transcribe-diarize 모델 처리 결과 전달</li>
 *   <li>대화 정제/요약 처리의 입력 데이터</li>
 * </ul>
 *
 * <p>주의:</p>
 * <ul>
 *   <li>success=false 인 경우 diarizedJson 값은 의미를 갖지 않음</li>
 * </ul>
 */
@AllArgsConstructor
@Getter
@Builder
public class TranscribeDiarizeResult {
    private boolean success;
    private String diarizedJson;
}

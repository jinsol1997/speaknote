package com.speaknote.speaknote.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;


/**
 * 사용자 오디오 히스토리 조회 결과를 나타내는 DTO
 *
 * <p>용도:</p>
 * <ul>
 *   <li>사용자의 음성 처리 히스토리 목록 조회 결과 전달</li>
 *   <li>UI/API 응답용 데이터 구조</li>
 * </ul>
 */
@Getter
@Builder
public class HistoryResult {
    private long idx;
    private String dialogue;
    private String summary;
    private LocalDateTime created;
}

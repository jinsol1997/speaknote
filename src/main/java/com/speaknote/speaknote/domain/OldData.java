package com.speaknote.speaknote.domain;

import lombok.Getter;


/**
 * 만료된 오디오 데이터 정리를 위해 사용하는 도메인
 *
 * <p>용도:</p>
 * <ul>
 *   <li>스케줄러에서 만료 대상 조회 결과로 사용</li>
 *   <li>파일 삭제 및 DB 정리를 위한 최소 정보 전달</li>
 * </ul>
 */
@Getter
public class OldData {
    private long idx;
    private String filePath;
}

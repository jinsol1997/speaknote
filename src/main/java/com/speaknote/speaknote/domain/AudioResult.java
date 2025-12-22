package com.speaknote.speaknote.domain;

import lombok.*;

import java.time.LocalDateTime;


/**
 * 음성 처리 결과를 나타내는 도메인
 *
 * <p>용도:</p>
 * <ul>
 *   <li>음성 처리 결과(DB 저장 대상)</li>
 * </ul>
 *
 * <p>주의:</p>
 * <ul>
 *   <li>idx 값은 DB insert 시 MyBatis useGeneratedKeys를 통해 설정됨</li>
 * </ul>
 */
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class AudioResult {
    private long idx;
    private String recorderId;
    private String dialogue;
    private String summary;
    private String fileName;
    private String filePath;
    private long fileSize;
    private LocalDateTime created;
}

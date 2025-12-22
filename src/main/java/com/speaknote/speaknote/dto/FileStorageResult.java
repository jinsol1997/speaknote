package com.speaknote.speaknote.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;


/**
 * 파일 저장 처리 결과를 나타내는 DTO
 *
 * <p>용도:</p>
 * <ul>
 *   <li>파일 업로드 처리 결과 전달</li>
 * </ul>
 *
 * <p>주의:</p>
 * <ul>
 *   <li>success=false 인 경우 fileName, filePath, fileSize 값은 의미를 갖지 않음</li>
 * </ul>
 */
@AllArgsConstructor
@Getter
@Builder
public class FileStorageResult {
    private boolean success;
    private String fileName;
    private String filePath;
    private long fileSize;
}

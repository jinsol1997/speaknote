package com.speaknote.speaknote.service.storage;

import com.speaknote.speaknote.dto.FileStorageResult;
import org.springframework.web.multipart.MultipartFile;


/**
 * 오디오 파일 저장소에 대한 인터페이스
 *
 * <p>역할:</p>
 * <ul>
 *   <li>음성 파일을 저장하고 접근 가능한 경로 정보를 제공</li>
 *   <li>저장된 파일을 경로 기준으로 삭제</li>
 * </ul>
 *
 * <p>설계 정책:</p>
 * <ul>
 *   <li>저장소의 종류에 의존하지 않도록 추상화</li>
 *   <li>저장 실패는 예외가 아닌 결과 객체 또는 boolean 값으로 표현</li>
 * </ul>
 *
 * <p>주의:</p>
 * <ul>
 *   <li>upload 성공 이후 DB 저장 실패 시, 상위 계층에서 deleteByPath를 호출해
 *       정합성을 맞춰야 함</li>
 * </ul>
 */
public interface FileStorageService {
    FileStorageResult upload(MultipartFile file,  String recorder_id);
    boolean deleteByPath(String filePath);
}

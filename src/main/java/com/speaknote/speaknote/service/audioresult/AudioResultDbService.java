package com.speaknote.speaknote.service.audioresult;

import com.speaknote.speaknote.domain.AudioResult;
import com.speaknote.speaknote.domain.OldData;
import com.speaknote.speaknote.mapper.AudioResultMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * AudioResult 도메인에 대한 DB 접근을 담당하는 서비스
 *
 * <p>역할:</p>
 * <ul>
 *   <li>오디오 처리 결과 저장</li>
 *   <li>사용자별 히스토리 조회</li>
 *   <li>만료 데이터 조회 및 삭제</li>
 * </ul>
 *
 * <p>설계 원칙:</p>
 * <ul>
 *   <li>DB 조회 실패와 정상 조회 결과를 구분하기 위해 null 또는 음수 값을 반환</li>
 *   <li>조회 결과가 0건인 경우에는 빈 리스트를 반환</li>
 *   <li>예외는 상위로 전파하지 않고 로그를 남긴 뒤 명시적인 실패 값을 반환</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AudioResultDbService {

    private final AudioResultMapper audioResultMapper;


    /**
     * AudioResult 데이터를 DB에 저장
     *
     * <p>처리 정책:</p>
     * <ul>
     *   <li>insert 결과 row 수가 1이 아니면 실패로 간주</li>
     *   <li>예외 발생 시 false 를 반환</li>
     * </ul>
     * <p>주의:</p>
     *  <ul>
     *    <li>저장 성공 시 AudioResult 객체의 idx 필드가 설정됨 (MyBatis useGeneratedKeys 사용)</li>
     *  </ul>
     *
     * @param audioResult 저장할 AudioResult 객체
     * @return 저장 성공 여부
     */
    public boolean save(AudioResult audioResult) {
        try {
            int rows = audioResultMapper.insert(audioResult);
            if (rows != 1) {
                log.error("AudioResult insert 실패 recorderId={}, fileName={}, fileSize={}",
                        audioResult.getRecorderId(), audioResult.getFileName(), audioResult.getFileSize());
                return false;
            }

            log.info("AudioResult insert 성공 recorderId={}, fileName={}, fileSize={}",
                    audioResult.getRecorderId(), audioResult.getFileName(), audioResult.getFileSize());
            return true;

        } catch (Exception e) {
            log.error("AudioResult insert 예외 발생 recorderId={}, fileName={}, fileSize={}",
                    audioResult.getRecorderId(), audioResult.getFileName(), audioResult.getFileSize(), e);
            return false;
        }
    }


    /**
     * 만료 기준에 해당하는 데이터 목록을 조회
     *
     * <p>반환 규칙:</p>
     * <ul>
     *   <li>조회 성공 시: 만료 대상 목록 (0건이면 빈 리스트)</li>
     *   <li>조회 실패 시: null 반환</li>
     * </ul>
     *
     * @return 만료 대상 데이터 목록 또는 null
     */
    public List<OldData> findOldData() {
        try {
            List<OldData> oldDataList = audioResultMapper.findOldData();
            log.info("OldData 조회 성공");
            return oldDataList;
        } catch (Exception e) {
            log.error("OldData 조회 예외 발생", e);
            return null;
        }
    }


    /**
     * 만료 데이터 idx 목록을 기준으로 DB에서 데이터를 삭제
     *
     * <p>반환 규칙:</p>
     * <ul>
     *   <li>삭제 성공 시: 삭제된 row 수 반환</li>
     *   <li>삭제 실패 또는 예외 발생 시: -1 반환</li>
     * </ul>
     *
     * @param idxList 삭제 대상 idx 목록
     * @return 삭제된 row 수 또는 -1
     */
    public int deleteOldDataByIdxList(List<Long> idxList) {
        try {
            int rows = audioResultMapper.deleteOldDataByIdxList(idxList);
            log.info("만료 데이터 리스트 삭제 성공");
            return rows;
        } catch (Exception e) {
            log.error("만료 데이터 리스트 삭제 예외 발생 idxList : {}", idxList, e);
            return -1;
        }
    }


    /**
     * 특정 사용자(recorderId)의 음성 처리 히스토리를 조회
     *
     * <p>조회 방식:</p>
     * <ul>
     *   <li>idx 기준 내림차순 조회</li>
     *   <li>lastIdx 보다 작은 데이터만 조회</li>
     *   <li>limit 개수만큼 조회</li>
     * </ul>
     *
     * <p>반환 규칙:</p>
     * <ul>
     *   <li>조회 성공 시: 히스토리 목록 (0건이면 빈 리스트)</li>
     *   <li>조회 실패 시: null 반환</li>
     * </ul>
     *
     * @param recorderId 사용자 식별자
     * @param lastIdx    마지막으로 조회한 히스토리 idx
     * @param limit      조회할 최대 개수
     * @return 히스토리 목록 또는 null
     */
    public List<AudioResult> findHistory(String recorderId, long lastIdx, int limit) {
        
        try {
            List<AudioResult> audioResultList = audioResultMapper.findHistoryByRecorderIdBeforeIdx(recorderId, lastIdx, limit);
            log.info("사용자 history 조회 성공");
            return audioResultList;
        } catch (Exception e) {
            log.error("사용자 history 조회 중 예외 발생 : {}, {}, {}", recorderId, lastIdx, limit, e);
            return null;
        }

    }
}

package com.speaknote.speaknote.mapper;

import com.speaknote.speaknote.domain.AudioResult;
import com.speaknote.speaknote.domain.OldData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * AudioResult 도메인에 대한 MyBatis Mapper 인터페이스
 *
 * <p>역할:</p>
 * <ul>
 *   <li>오디오 처리 결과(AudioResult) DB 저장</li>
 *   <li>만료 대상 데이터(OldData) 조회 및 삭제</li>
 *   <li>사용자별 오디오 히스토리 조회</li>
 * </ul>
 */
@Mapper
public interface AudioResultMapper {


    /**
     * AudioResult 데이터를 DB에 저장
     *
     * <p>동작:</p>
     * <ul>
     *   <li>AudioResult 객체로 INSERT 수행</li>
     *   <li>DB에서 생성된 PK(idx)를 AudioResult.idx 필드에 저장함 (useGeneratedKeys 사용)</li>
     * </ul>
     *
     * <p>반환 규칙:</p>
     * <ul>
     *   <li>성공 시: 1 반환</li>
     *   <li>실패 시: 0 또는 그 외 값 반환</li>
     * </ul>
     *
     * @param audioResult 저장할 AudioResult 객체
     * @return insert된 row 수
     */
    int insert(AudioResult audioResult);


    /**
     * 만료 기준에 해당하는 데이터 목록을 조회
     *
     * <p>동작:</p>
     * <ul>
     *   <li>현재 시점을 기준으로 만료된(7일 경과) 오디오 데이터 조회</li>
     * </ul>
     *
     * <p>반환 규칙:</p>
     * <ul>
     *   <li>만료 대상이 없으면 빈 리스트 반환</li>
     * </ul>
     *
     * @return 만료 대상 데이터 목록
     */
    List<OldData> findOldData();


    /**
     * idx 목록을 기준으로 만료 데이터를 DB에서 삭제
     *
     * <p>동작:</p>
     * <ul>
     *   <li>전달받은 idx 목록에 해당하는 데이터 DELETE</li>
     * </ul>
     *
     * <p>반환 규칙:</p>
     * <ul>
     *   <li>삭제된 row 수 반환</li>
     * </ul>
     *
     * @param idxList 삭제 대상 idx 목록
     * @return 삭제된 row 수
     */
    int deleteOldDataByIdxList(List<Long> idxList);


    /**
     * 특정 사용자의 오디오 처리 히스토리를 조회
     *
     * <p>조회 조건:</p>
     * <ul>
     *   <li>recorderId 기준 조회</li>
     *   <li>lastIdx 보다 작은 데이터만 조회 (이전 데이터)</li>
     *   <li>idx 내림차순 정렬</li>
     *   <li>limit 개수만큼 조회</li>
     * </ul>
     *
     * <p>반환 규칙:</p>
     * <ul>
     *   <li>조회 결과가 없으면 빈 리스트 반환</li>
     * </ul>
     *
     * @param recorderId 사용자 식별자
     * @param lastIdx    마지막으로 조회한 히스토리 idx
     * @param limit      조회할 최대 개수
     * @return 오디오 히스토리 목록
     */
    List<AudioResult> findHistoryByRecorderIdBeforeIdx(@Param("recorderId") String recorderId,
                                                       @Param("lastIdx") long lastIdx,
                                                       @Param("limit") int limit);
}

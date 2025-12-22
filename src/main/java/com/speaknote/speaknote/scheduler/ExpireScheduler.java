package com.speaknote.speaknote.scheduler;

import com.speaknote.speaknote.domain.OldData;
import com.speaknote.speaknote.service.audioresult.AudioResultDbService;
import com.speaknote.speaknote.service.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


/**
 * 만료된 오디오 파일 및 DB를 정리하는 스케줄러.
 *
 * <p>주요 역할:</p>
 * <ul>
 *   <li>DB에서 만료 대상(OldData) 조회</li>
 *   <li>스토리지에서 파일 삭제</li>
 *   <li>파일 삭제에 성공한 대상만 DB에서 삭제</li>
 * </ul>
 *
 * <p>실행 정책:</p>
 * <ul>
 *   <li>매일 00:00에 1회 실행</li>
 * </ul>
 *
 * <p>주의:</p>
 * <ul>
 *   <li>파일 삭제와 DB 삭제는 분리되어 있으며, 파일 삭제 성공 건만 DB에서 정리한다.</li>
 *   <li>파일 삭제 건수와 DB 삭제 row 수가 불일치할 수 있어 경고 로그를 남긴다.</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExpireScheduler {

    private final FileStorageService fileStorageService;
    private final AudioResultDbService audioResultDbService;


    /**
     * 만료 데이터 정리 작업을 수행한다.
     *
     * <p>실행 시각:</p>
     * <ul>
     *   <li>cron = "0 0 0 * * *" (매일 00:00)</li>
     * </ul>
     *
     * <p>처리 흐름:</p>
     * <ul>
     *   <li>만료 대상 DB에서 조회</li>
     *   <li>각 대상의 파일 삭제 시도</li>
     *   <li>파일 삭제 성공한 idx 목록 구성</li>
     *   <li>성공 idx 목록에 대해서만 DB 삭제 수행</li>
     * </ul>
     *
     * <p>결과 처리:</p>
     * <ul>
     *   <li>파일 삭제 전체 실패 시 DB 삭제를 수행하지 않는다.</li>
     *   <li>DB 삭제 row 수가 파일 삭제 성공 건수와 다르면 경고 로그를 남긴다.</li>
     * </ul>
     */
    @Scheduled(cron = "0 0 0 * * *")    // 초 분 시 일 월 요일 **0*** 하면 0:00 ~ 0:59 까지 매초 실행됨
    public void expire(){
        
        log.info("스케쥴러 시작");

        List<OldData> oldDataList = audioResultDbService.findOldData();
        if (oldDataList == null) {
            return;
        } else if (oldDataList.isEmpty()) {
            log.info("기한 만료된 데이터 없음");
            return;
        }

        List<Long> successIdxList = new ArrayList<>();

        for (OldData oldData : oldDataList) {
            boolean isSuccess = fileStorageService.deleteByPath(oldData.getFilePath());
            if (isSuccess) {
                successIdxList.add(oldData.getIdx());
            }
        }

        if(successIdxList.isEmpty()){
            log.warn("파일 삭제 전체 실패");
            return;
        }

        int result = audioResultDbService.deleteOldDataByIdxList(successIdxList);
        if(result < 0) {
            return;
        }

        if(result != successIdxList.size()){
            log.warn("파일 삭제 수와 db 삭제 row수 불일치 - 파일 삭제 : {}건, db 삭제 : {}건", successIdxList.size(), result);
        } else {
            log.info("만료 데이터 리스트 삭제 완료 총 {}건", result);
        }

    }

}

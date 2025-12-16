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

@Slf4j
@Component
@RequiredArgsConstructor
public class ExpireScheduler {

    private final FileStorageService fileStorageService;
    private final AudioResultDbService audioResultDbService;

    // 초 분 시 일 월 요일 **0*** 하면 0:00 ~ 0:59 까지 매초 실행됨
    @Scheduled(cron = "0 0 0 * * *")
    public void expire(){
        
        log.info("스케쥴러 시작");

        List<OldData> oldDataList = audioResultDbService.findOldData();
        if (oldDataList.isEmpty()) {
            log.info("기한 만료된 데이터 없음");
            return;
        }

        List<Integer> successIdxList = new ArrayList<>();

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

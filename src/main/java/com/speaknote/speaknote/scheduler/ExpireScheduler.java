package com.speaknote.speaknote.scheduler;

import com.speaknote.speaknote.domain.OldData;
import com.speaknote.speaknote.service.audioresult.AudioResultDbService;
import com.speaknote.speaknote.service.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ExpireScheduler {

    private final FileStorageService fileStorageService;
    private final AudioResultDbService audioResultDbService;

    // 초 분 시 일 월 요일 **0*** 하면 0:00 ~ 0:59 까지 매초 실행됨
    @Scheduled(cron = "0 0 0 * * *")
    public void expire(){

        List<OldData> oldDataList = audioResultDbService.findOldData();
        if (oldDataList == null || oldDataList.isEmpty()) {
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
            return;
        }

        int result = audioResultDbService.deleteOldDataByIdxList(successIdxList);

    }

}

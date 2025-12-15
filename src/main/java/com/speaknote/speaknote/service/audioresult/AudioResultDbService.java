package com.speaknote.speaknote.service.audioresult;

import com.speaknote.speaknote.domain.AudioResult;
import com.speaknote.speaknote.domain.OldData;
import com.speaknote.speaknote.mapper.AudioResultMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AudioResultDbService {

    private final AudioResultMapper audioResultMapper;

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

    public List<OldData> findOldData() {
        try {
            List<OldData> oldDataList = audioResultMapper.findOldData();
            log.info("OldData 조회 성공");
            return oldDataList;
        } catch (Exception e) {
            log.error("OldData 조회 예외 발생", e);
            return List.of();
        }
    }

    public int deleteOldDataByIdxList(List<Integer> idxList) {
        try {
            int rows = audioResultMapper.deleteOldDataByIdxList(idxList);
            log.info("만료 데이터 리스트 삭제 성공");
            return rows;
        } catch (Exception e) {
            log.error("만료 데이터 리스트 삭제 예외 발생 idxList : {}", idxList, e);
            return -1;
        }
    }
}

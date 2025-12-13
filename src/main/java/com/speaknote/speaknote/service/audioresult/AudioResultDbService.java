package com.speaknote.speaknote.service.audioresult;

import com.speaknote.speaknote.domain.AudioResult;
import com.speaknote.speaknote.domain.OldData;
import com.speaknote.speaknote.mapper.AudioResultMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AudioResultDbService {

    private final AudioResultMapper audioResultMapper;

    public boolean save(AudioResult audioResult) {
        return audioResultMapper.insert(audioResult) == 1;
    }

    public List<OldData> findOldData() {
        return audioResultMapper.findOldData();
    }

    public int deleteOldDataByIdxList(List<Integer> idxList) {
        return audioResultMapper.deleteOldDataByIdxList(idxList);
    }
}

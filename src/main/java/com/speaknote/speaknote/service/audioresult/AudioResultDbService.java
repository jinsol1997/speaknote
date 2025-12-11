package com.speaknote.speaknote.service.audioresult;

import com.speaknote.speaknote.domain.AudioResult;
import com.speaknote.speaknote.mapper.AudioResultMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AudioResultDbService {

    private final AudioResultMapper audioResultMapper;

    public boolean save(AudioResult audioResult) {
        return audioResultMapper.insert(audioResult) == 1;
    }
}

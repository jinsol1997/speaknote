package com.speaknote.speaknote.mapper;

import com.speaknote.speaknote.domain.AudioResult;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AudioResultMapper {
    int insert(AudioResult audioResult);
}

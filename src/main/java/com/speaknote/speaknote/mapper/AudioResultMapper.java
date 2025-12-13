package com.speaknote.speaknote.mapper;

import com.speaknote.speaknote.domain.AudioResult;
import com.speaknote.speaknote.domain.OldData;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface AudioResultMapper {
    int insert(AudioResult audioResult);
    List<OldData> findOldData();
    int deleteOldDataByIdxList(List<Integer> idxList);
}

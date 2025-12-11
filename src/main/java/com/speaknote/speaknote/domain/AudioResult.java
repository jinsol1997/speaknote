package com.speaknote.speaknote.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AudioResult {
    private int idx;
    private String recorderId;
    private String dialogue;
    private String summary;
    private String fileName;
    private String filePath;
    private long fileSize;
    private LocalDateTime created;
}

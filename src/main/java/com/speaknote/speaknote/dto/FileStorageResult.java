package com.speaknote.speaknote.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class FileStorageResult {
    private boolean success;
    private String fileName;
    private String filePath;
    private long fileSize;
}

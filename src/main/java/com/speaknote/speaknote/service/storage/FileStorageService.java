package com.speaknote.speaknote.service.storage;

import com.speaknote.speaknote.dto.FileStorageResult;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    FileStorageResult upload(MultipartFile file,  String recorder_id);
    boolean deleteByPath(String filePath);
}

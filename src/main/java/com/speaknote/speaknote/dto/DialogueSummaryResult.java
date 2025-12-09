package com.speaknote.speaknote.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class DialogueSummaryResult {
    private boolean success;
    private String errorMessage;
    private String dialogue;
    private String summary;
}

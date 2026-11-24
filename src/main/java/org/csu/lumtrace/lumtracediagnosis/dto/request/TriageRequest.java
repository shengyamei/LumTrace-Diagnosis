package org.csu.lumtrace.lumtracediagnosis.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * AI导诊分诊请求
 */
@Data
public class TriageRequest {
    /** 症状描述文本（口语化输入） */
    @NotBlank(message = "请描述您的症状")
    private String symptomText;
}

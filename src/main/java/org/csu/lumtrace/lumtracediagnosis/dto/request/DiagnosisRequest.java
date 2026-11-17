package org.csu.lumtrace.lumtracediagnosis.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 提交 AI 诊断请求
 */
@Data
public class DiagnosisRequest implements Serializable {
    /** 用户选择的症状ID */
    @NotEmpty(message = "至少选择一个症状")
    private List<Long> symptomIds;

    /** 自述 */
    private String selfDescription;

    private Integer age;
    /** 0未知 1男 2女 */
    private Integer gender;
}

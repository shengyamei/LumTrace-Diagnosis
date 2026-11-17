package org.csu.lumtrace.lumtracediagnosis.entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI 诊断记录
 */
@Data
public class DiagnosisRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    /** 症状ID列表（逗号分隔） */
    private String symptomIds;
    /** 用户自述 */
    private String selfDescription;
    private Integer age;
    private Integer gender;
    /** AI 结论疾病 */
    private String aiDisease;
    /** AI 置信度 */
    private BigDecimal confidence;
    /** AI 建议 */
    private String aiSuggestion;
    /** 命中的可能疾病列表 JSON */
    private String candidateDiseases;
    /** 状态：PENDING/REVIEWED/CONFIRMED */
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

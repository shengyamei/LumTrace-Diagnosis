package org.csu.lumtrace.lumtracediagnosis.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 诊断报告（可由医生复核）
 */
@Data
public class DiagnosisReport implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long recordId;
    private Long userId;
    private String reportNo;
    private String content;
    private Long doctorId;
    private String doctorReview;
    /** 状态：AI_ONLY / DOCTOR_REVIEWED / SIGNED */
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

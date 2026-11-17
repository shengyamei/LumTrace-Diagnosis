package org.csu.lumtrace.lumtracediagnosis.entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 医学知识库条目：疾病 - 症状 权重映射
 */
@Data
public class MedicalKnowledge implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long diseaseId;
    private String diseaseName;
    private Long symptomId;
    /** 该症状对该疾病的权重（0~1） */
    private BigDecimal weight;
    /** 是否需要"必须具备"（必备症状） */
    private Integer required;
}

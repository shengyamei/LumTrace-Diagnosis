package org.csu.lumtrace.lumtracediagnosis.entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 病程记录
 */
@Data
public class DiseaseRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    /** 记录时间 */
    private LocalDateTime recordTime;
    /** 症状描述 */
    private String symptom;
    /** 体温 */
    private BigDecimal temperature;
    /** 疼痛评分 1-10 */
    private Integer painScore;
    /** 备注 */
    private String remark;
    /** 照片URL（后置功能） */
    private String imgUrl;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

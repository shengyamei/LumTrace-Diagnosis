package org.csu.lumtrace.lumtracediagnosis.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 症状字典实体
 */
@Data
public class Symptom implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    /** 分类：如 呼吸系统 / 消化系统 / 神经系统 ... */
    private String category;
    private String description;
    /** 常见严重度：1-轻 2-中 3-重 */
    private Integer defaultSeverity;
    private Integer status;
    private LocalDateTime createTime;
}

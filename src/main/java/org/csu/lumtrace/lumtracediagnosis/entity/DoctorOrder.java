package org.csu.lumtrace.lumtracediagnosis.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 医嘱信息
 */
@Data
public class DoctorOrder implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    /** 药品名称 */
    private String drugName;
    /** 剂量 */
    private String dosage;
    /** 频次（如：每天2次） */
    private String frequency;
    /** 提醒时间（如：08:00,20:00） */
    private String remindTime;
    /** 开始日期 */
    private LocalDate startDate;
    /** 结束日期 */
    private LocalDate endDate;
    /** 是否已完成疗程 0否 1是 */
    private Integer isFinish;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

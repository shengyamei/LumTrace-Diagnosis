package org.csu.lumtrace.lumtracediagnosis.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 科室分诊规则
 */
@Data
public class DeptRule implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    /** 症状关键词 */
    private String keyword;
    /** 推荐科室 */
    private String deptName;
    /** 就医提示 */
    private String tip;
    private LocalDateTime createTime;
}

package org.csu.lumtrace.lumtracediagnosis.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 科室分诊规则（对应 t_dept_rule，架构说明书 2.5）
 * <p>
 * 关键词 -> 推荐科室 + 就医提示，规则命中即直接返回，不经大模型。
 */
@Data
public class DeptRule implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    /** 症状关键词（口语化优先，如「喉咙疼」「拉肚子」） */
    private String keyword;
    /** 推荐科室 */
    private String deptName;
    /** 就医提示 */
    private String tip;
}

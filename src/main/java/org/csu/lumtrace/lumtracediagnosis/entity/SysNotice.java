package org.csu.lumtrace.lumtracediagnosis.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统公告/免责声明
 */
@Data
public class SysNotice implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    /** 内容 */
    private String content;
    /** 类型：DISCLAIMER/NOTICE */
    private String type;
    private LocalDateTime createTime;
}

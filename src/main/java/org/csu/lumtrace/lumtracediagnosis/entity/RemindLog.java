package org.csu.lumtrace.lumtracediagnosis.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 提醒记录
 */
@Data
public class RemindLog implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    /** 医嘱ID */
    private Long orderId;
    /** 用户ID */
    private Long userId;
    /** 提醒时间 */
    private LocalDateTime remindTime;
    /** 是否已读 0否 1是 */
    private Integer isRead;
    /** 是否已服药 0否 1是 */
    private Integer isTaken;
    private LocalDateTime createTime;
}

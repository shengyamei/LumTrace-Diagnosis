package org.csu.lumtrace.lumtracediagnosis.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 医生实体
 */
@Data
public class Doctor implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    private String name;
    private String department;
    /** 职称：住院医生 / 主治 / 副主任 / 主任 */
    private String title;
    private String hospital;
    private String specialty;
    private String introduction;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

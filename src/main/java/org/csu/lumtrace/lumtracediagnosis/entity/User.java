package org.csu.lumtrace.lumtracediagnosis.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户（患者）实体
 */
@Data
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String phone;
    private String email;
    /** 性别：0-未知 1-男 2-女 */
    private Integer gender;
    private LocalDate birthDate;
    /** 角色：PATIENT / DOCTOR / ADMIN */
    private String role;
    /** 状态：0-禁用 1-启用 */
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

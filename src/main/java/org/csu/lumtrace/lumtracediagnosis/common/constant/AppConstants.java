package org.csu.lumtrace.lumtracediagnosis.common.constant;

/**
 * 全局常量
 */
public final class AppConstants {
    private AppConstants() {}

    /** 角色 */
    public static final String ROLE_PATIENT = "PATIENT";
    public static final String ROLE_DOCTOR = "DOCTOR";
    public static final String ROLE_ADMIN = "ADMIN";

    /** 诊断记录状态 */
    public static final String RECORD_PENDING = "PENDING";
    public static final String RECORD_REVIEWED = "REVIEWED";
    public static final String RECORD_CONFIRMED = "CONFIRMED";

    /** 报告状态 */
    public static final String REPORT_AI_ONLY = "AI_ONLY";
    public static final String REPORT_DOCTOR_REVIEWED = "DOCTOR_REVIEWED";
    public static final String REPORT_SIGNED = "SIGNED";

    /** 通用状态 */
    public static final int STATUS_DISABLED = 0;
    public static final int STATUS_ENABLED = 1;
}

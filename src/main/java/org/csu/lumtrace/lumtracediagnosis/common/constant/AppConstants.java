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

    /** 免责声明（架构说明书 1.1 固定文案，全系统唯一来源） */
    public static final String DISCLAIMER =
            "本应用内容仅供参考，不能替代执业医师诊疗，如有紧急情况请立即拨打 120。";

    /** 敏感词命中后的标准安全话术（替代违规输出） */
    public static final String SAFE_SUGGESTION =
            "建议前往医院对应科室就诊，由医生面诊后判断。";

    /** 引擎类型标识 */
    public static final String ENGINE_RULE = "RULE";
    public static final String ENGINE_LLM = "LLM";
}

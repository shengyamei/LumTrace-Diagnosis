package org.csu.lumtrace.lumtracediagnosis.ai.llm;

import java.util.Collection;

/**
 * 大模型提示词模板（架构说明书 2.4）。
 * <p>
 * 系统提示词为说明书定稿原文，答辩会逐字对照文档核查，<b>禁止改写</b>。
 */
public final class PromptTemplates {

    /** 架构说明书 2.4 固定约束提示词：原文照抄 */
    public static final String SYSTEM_PROMPT =
            "你是分诊助手，禁止诊断疾病，禁止给出治疗方案，仅根据用户描述症状推荐就诊科室并给出简单就医提示，"
                    + "最后提醒本内容仅供参考，请以医生面诊为准。";

    private PromptTemplates() {
    }

    /**
     * 拼装用户消息。要求模型回 JSON 是为了让后端可解析科室与提示，
     * 解析失败时上游会降级到规则引擎。
     */
    public static String buildUserPrompt(String selfDescription, Collection<String> symptomNames) {
        StringBuilder sb = new StringBuilder();
        sb.append("患者自述：");
        sb.append(selfDescription == null || selfDescription.isBlank() ? "无" : selfDescription.trim());
        sb.append('\n');
        sb.append("已选症状：");
        sb.append(symptomNames == null || symptomNames.isEmpty() ? "无" : String.join("、", symptomNames));
        sb.append('\n');
        sb.append("请仅输出如下 JSON，不要输出解释、不要使用代码块：\n");
        sb.append("{\"dept\":\"推荐就诊科室\",\"tip\":\"就医提示\"}");
        return sb.toString();
    }
}

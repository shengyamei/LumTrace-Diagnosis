package org.csu.lumtrace.lumtracediagnosis.ai;

import org.csu.lumtrace.lumtracediagnosis.config.AiConfig;
import org.csu.lumtrace.lumtracediagnosis.dto.request.DiagnosisRequest;
import org.csu.lumtrace.lumtracediagnosis.dto.response.DiagnosisResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;

/**
 * 大模型诊断引擎（占位实现，可按需接入 OpenAI/文心/通义等）。
 * <p>
 * 默认返回 UNKNOWN，只有 {@code lumtrace.ai.engine=LLM} 且配置了 apiKey 时才会被上层选择。
 * 生产接入方式：
 * <ol>
 *   <li>RestTemplate/WebClient 调用 {@link AiConfig.Llm#getBaseUrl()}/chat/completions</li>
 *   <li>将 {@link DiagnosisRequest#getSelfDescription()} 与症状列表拼成 Prompt</li>
 *   <li>解析返回 JSON 到 {@link DiagnosisResponse}</li>
 * </ol>
 */
@Component
public class LlmDiagnosisEngine implements AiDiagnosisEngine {
    private static final Logger log = LoggerFactory.getLogger(LlmDiagnosisEngine.class);

    @Autowired
    private AiConfig aiConfig;

    @Override
    public DiagnosisResponse diagnose(DiagnosisRequest request, Map<Long, String> symptomNames) {
        if (aiConfig.getLlm().getApiKey() == null || aiConfig.getLlm().getApiKey().isBlank()) {
            log.warn("LLM apiKey 未配置，返回空结果");
        } else {
            log.info("TODO: 调用 {}/chat/completions, model={}", aiConfig.getLlm().getBaseUrl(), aiConfig.getLlm().getModel());
        }
        DiagnosisResponse resp = new DiagnosisResponse();
        resp.setTopDisease("UNKNOWN");
        resp.setConfidence(BigDecimal.ZERO);
        resp.setSeverity(1);
        resp.setSuggestion("LLM 引擎尚未接入，请配置 lumtrace.ai.llm.api-key 或切换到 RULE 引擎。");
        resp.setCandidates(Collections.emptyList());
        return resp;
    }

    @Override
    public String engineType() {
        return "LLM";
    }
}

package org.csu.lumtrace.lumtracediagnosis.ai;

import org.csu.lumtrace.lumtracediagnosis.ai.llm.LlmClient;
import org.csu.lumtrace.lumtracediagnosis.ai.llm.LlmUnavailableException;
import org.csu.lumtrace.lumtracediagnosis.ai.llm.PromptTemplates;
import org.csu.lumtrace.lumtracediagnosis.common.constant.AppConstants;
import org.csu.lumtrace.lumtracediagnosis.dto.request.DiagnosisRequest;
import org.csu.lumtrace.lumtracediagnosis.dto.response.DiagnosisResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 大模型分诊引擎（增强层，架构说明书 2.4）。
 * <p>
 * 只在规则库未命中时被调用；任何异常一律抛 {@link LlmUnavailableException}，
 * 由 {@link DiagnosisEngineRouter} 兜底回规则引擎，保证用户永远拿得到结果。
 * <p>
 * 输出只取「推荐科室 + 就医提示」，不输出疾病结论。
 */
@Component
public class LlmDiagnosisEngine implements AiDiagnosisEngine {
    private static final Logger log = LoggerFactory.getLogger(LlmDiagnosisEngine.class);

    @Autowired
    private LlmClient llmClient;

    @Override
    public DiagnosisResponse diagnose(DiagnosisRequest request, Map<Long, String> symptomNames) {
        if (!llmClient.isAvailable()) {
            throw new LlmUnavailableException(
                    llmClient.isOffline() ? "离线模式已开启" : "大模型 apiKey 未配置");
        }

        String userPrompt = PromptTemplates.buildUserPrompt(request.getSelfDescription(), symptomNames.values());
        String content = llmClient.chat(userPrompt);

        String dept = extractField(content, "dept");
        String tip = extractField(content, "tip");
        if (dept == null && tip == null) {
            throw new LlmUnavailableException("大模型返回无法解析为科室/提示: " + content);
        }
        log.info("大模型分诊结果: dept={}, tip={}", dept, tip);

        DiagnosisResponse resp = new DiagnosisResponse();
        resp.setDeptName(dept);
        resp.setTip(tip);
        // 合规约束：AI 层不输出疾病结论，疾病相关字段保持空值语义
        resp.setTopDisease("UNKNOWN");
        resp.setConfidence(BigDecimal.ZERO);
        resp.setSeverity(1);
        resp.setSuggestion(tip != null ? tip : content);
        resp.setCandidates(Collections.emptyList());
        resp.setDisclaimer(AppConstants.DISCLAIMER);
        return resp;
    }

    /** 从模型输出中抽取 JSON 字段；容忍 markdown 代码块包裹与多余解释文字 */
    private static String extractField(String text, String field) {
        if (text == null || text.isBlank()) return null;
        Pattern p = Pattern.compile("\"" + Pattern.quote(field) + "\"\\s*:\\s*\"([^\"]*)\"");
        Matcher m = p.matcher(text);
        return m.find() ? m.group(1) : null;
    }

    @Override
    public String engineType() {
        return AppConstants.ENGINE_LLM;
    }
}

package org.csu.lumtrace.lumtracediagnosis.ai;

import org.csu.lumtrace.lumtracediagnosis.dto.request.DiagnosisRequest;
import org.csu.lumtrace.lumtracediagnosis.dto.response.DiagnosisResponse;

import java.util.List;

/**
 * AI 诊断引擎顶层接口
 * <p>
 * 支持两种实现：
 * <ul>
 *   <li>{@link RuleBasedDiagnosisEngine} 内置加权规则引擎（离线可用）</li>
 *   <li>{@link LlmDiagnosisEngine} 大模型接入（可选）</li>
 * </ul>
 */
public interface AiDiagnosisEngine {

    /**
     * 执行诊断
     *
     * @param request   诊断请求（含症状ID、自述、年龄、性别）
     * @param symptomNames 已解析的症状名称，key=symptomId, value=name
     * @return 诊断结果
     */
    DiagnosisResponse diagnose(DiagnosisRequest request, java.util.Map<Long, String> symptomNames);

    /** 引擎类型标识 */
    String engineType();

    /** 支持的候选疾病数量 */
    default int topK() {
        return 5;
    }

    /**
     * 便捷方法：从 request 里提取症状ID列表
     */
    default List<Long> extractSymptomIds(DiagnosisRequest request) {
        return request.getSymptomIds();
    }
}

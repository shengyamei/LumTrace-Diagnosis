package org.csu.lumtrace.lumtracediagnosis.ai;

import org.csu.lumtrace.lumtracediagnosis.dto.request.DiagnosisRequest;
import org.csu.lumtrace.lumtracediagnosis.dto.response.DiagnosisResponse;
import org.csu.lumtrace.lumtracediagnosis.entity.Symptom;
import org.csu.lumtrace.lumtracediagnosis.mapper.SymptomMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 引擎路由：根据配置选择 RULE 或 LLM。默认使用 RULE，保证项目开箱可用。
 */
@Service
@Primary
public class DiagnosisEngineRouter implements AiDiagnosisEngine {

    @Autowired
    @Qualifier("ruleBasedDiagnosisEngine")
    private AiDiagnosisEngine ruleEngine;

    @Autowired
    @Qualifier("llmDiagnosisEngine")
    private AiDiagnosisEngine llmEngine;

    @Autowired
    private SymptomMapper symptomMapper;

    @Autowired
    private org.csu.lumtrace.lumtracediagnosis.config.AiConfig aiConfig;

    @Override
    public DiagnosisResponse diagnose(DiagnosisRequest request, Map<Long, String> symptomNames) {
        AiDiagnosisEngine target = "LLM".equalsIgnoreCase(aiConfig.getEngine()) ? llmEngine : ruleEngine;
        return target.diagnose(request, symptomNames);
    }

    @Override
    public String engineType() {
        return aiConfig.getEngine();
    }

    /** 将请求中的 symptomIds 解析为 id->name 映射 */
    public Map<Long, String> resolveSymptomNames(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return Map.of();
        List<Symptom> list = symptomMapper.selectByIds(ids);
        return list.stream().collect(Collectors.toMap(Symptom::getId, Symptom::getName));
    }
}

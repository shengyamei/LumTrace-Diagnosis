package org.csu.lumtrace.lumtracediagnosis.ai;

import org.csu.lumtrace.lumtracediagnosis.common.constant.AppConstants;
import org.csu.lumtrace.lumtracediagnosis.dto.request.DiagnosisRequest;
import org.csu.lumtrace.lumtracediagnosis.dto.response.DiagnosisResponse;
import org.csu.lumtrace.lumtracediagnosis.entity.Symptom;
import org.csu.lumtrace.lumtracediagnosis.mapper.SymptomMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger log = LoggerFactory.getLogger(DiagnosisEngineRouter.class);

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
        // 离线降级：现场断网演示时一键切换，完全不发外部请求
        if (Boolean.TRUE.equals(aiConfig.getOfflineMode())) {
            log.info("离线降级模式开启，仅使用本地规则引擎");
            return ruleEngine.diagnose(request, symptomNames);
        }
        if (AppConstants.ENGINE_LLM.equalsIgnoreCase(aiConfig.getEngine())) {
            try {
                return llmEngine.diagnose(request, symptomNames);
            } catch (Exception e) {
                // 无 Key / 超时 / 返回不可解析 → 一律回落规则引擎，不向前端报错
                log.warn("大模型引擎不可用，降级到规则引擎: {}", e.getMessage());
                return ruleEngine.diagnose(request, symptomNames);
            }
        }
        return ruleEngine.diagnose(request, symptomNames);
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

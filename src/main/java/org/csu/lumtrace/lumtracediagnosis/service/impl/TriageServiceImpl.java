package org.csu.lumtrace.lumtracediagnosis.service.impl;

import org.csu.lumtrace.lumtracediagnosis.config.AiConfig;
import org.csu.lumtrace.lumtracediagnosis.dto.response.TriageResponse;
import org.csu.lumtrace.lumtracediagnosis.entity.DeptRule;
import org.csu.lumtrace.lumtracediagnosis.mapper.DeptRuleMapper;
import org.csu.lumtrace.lumtracediagnosis.service.TriageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI导诊分诊服务实现：规则库优先 → 大模型兜底 → 敏感词过滤
 */
@Service
public class TriageServiceImpl implements TriageService {
    private static final Logger log = LoggerFactory.getLogger(TriageServiceImpl.class);

    @Autowired
    private DeptRuleMapper deptRuleMapper;

    @Autowired
    private AiConfig aiConfig;

    /** 敏感词列表：大模型输出中出现这些词需替换为安全表述 */
    private static final Set<String> SENSITIVE_WORDS = Set.of(
            "确诊", "诊断为", "处方", "建议服用", "开药", "用药方案", "手术治疗", "必须服用"
    );

    private static final Pattern SENSITIVE_PATTERN = Pattern.compile(
            "(确诊|诊断为|处方|建议服用\\S+|开药|用药方案|手术治疗|必须服用\\S*)"
    );

    @Override
    public TriageResponse triage(String symptomText) {
        // 1. 优先匹配本地分诊规则库
        TriageResponse ruleResult = matchRules(symptomText);
        if (ruleResult != null) {
            log.info("分诊规则命中: input={}, dept={}", symptomText, ruleResult.getDeptName());
            return ruleResult;
        }

        // 2. 判断是否启用大模型（离线模式 or 引擎配置）
        if (aiConfig.isOfflineMode() || !"LLM".equalsIgnoreCase(aiConfig.getEngine())) {
            // 离线模式或未配置LLM，返回默认提示
            return TriageResponse.ofRule("全科/发热门诊",
                    "未能精确匹配科室，建议先到全科分诊，由医护人员引导至对应科室。本内容仅供参考，请以医生面诊为准。");
        }

        // 3. 调用大模型兜底
        try {
            TriageResponse llmResult = callLlm(symptomText);
            // 4. 敏感词过滤
            llmResult = filterSensitiveOutput(llmResult);
            return llmResult;
        } catch (Exception e) {
            log.error("大模型调用失败，降级为规则兜底: {}", e.getMessage());
            return TriageResponse.ofRule("全科/发热门诊",
                    "系统暂时无法精确分析，建议先到全科分诊。本内容仅供参考，请以医生面诊为准。");
        }
    }

    /**
     * 规则库匹配：将用户输入的文本与所有关键词做包含匹配
     */
    private TriageResponse matchRules(String symptomText) {
        List<DeptRule> allRules = deptRuleMapper.selectAll();
        DeptRule bestMatch = null;
        int bestLength = 0;

        for (DeptRule rule : allRules) {
            // 关键词包含在用户输入中
            if (symptomText.contains(rule.getKeyword())) {
                // 选择最长匹配（更精确）
                if (rule.getKeyword().length() > bestLength) {
                    bestMatch = rule;
                    bestLength = rule.getKeyword().length();
                }
            }
        }

        if (bestMatch != null) {
            String tip = bestMatch.getTip() + " 本内容仅供参考，请以医生面诊为准。";
            return TriageResponse.ofRule(bestMatch.getDeptName(), tip);
        }
        return null;
    }

    /**
     * 调用大模型进行分诊（实际项目接入通义千问/文心一言等API）
     */
    private TriageResponse callLlm(String symptomText) {
        // TODO: 接入真实LLM API（RestTemplate/WebClient调用AiConfig.Llm.baseUrl）
        // 当前占位实现，实际部署时替换为HTTP调用
        log.info("调用LLM分诊: model={}, input={}", aiConfig.getLlm().getModel(), symptomText);

        // 模拟返回 - 实际应解析LLM返回的JSON
        String mockDept = "全科/发热门诊";
        String mockTip = "根据您的描述，建议前往" + mockDept + "就诊。本内容仅供参考，请以医生面诊为准。";

        return TriageResponse.ofLlm(mockDept, mockTip);
    }

    /**
     * 敏感词过滤：拦截不当医疗表述
     */
    private TriageResponse filterSensitiveOutput(TriageResponse response) {
        if (response.getTip() == null) return response;

        Matcher matcher = SENSITIVE_PATTERN.matcher(response.getTip());
        if (matcher.find()) {
            log.warn("检测到敏感词输出，已过滤: {}", response.getTip());
            String filtered = matcher.replaceAll("建议就医检查");
            response.setTip(filtered);
        }
        return response;
    }
}

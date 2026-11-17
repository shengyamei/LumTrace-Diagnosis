package org.csu.lumtrace.lumtracediagnosis.ai;

import jakarta.annotation.PostConstruct;
import org.csu.lumtrace.lumtracediagnosis.config.AiConfig;
import org.csu.lumtrace.lumtracediagnosis.dto.request.DiagnosisRequest;
import org.csu.lumtrace.lumtracediagnosis.dto.response.DiagnosisResponse;
import org.csu.lumtrace.lumtracediagnosis.entity.MedicalKnowledge;
import org.csu.lumtrace.lumtracediagnosis.mapper.MedicalKnowledgeMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 加权规则引擎：基于疾病-症状权重矩阵的相似度打分。
 * <p>
 * 打分公式：
 * <pre>
 * score(d) = Σ (w_i · hit_i) / Σ w_i
 * 若必备症状未命中 → score *= 0.4 惩罚
 * </pre>
 */
@Component
public class RuleBasedDiagnosisEngine implements AiDiagnosisEngine {
    private static final Logger log = LoggerFactory.getLogger(RuleBasedDiagnosisEngine.class);

    @Autowired
    private MedicalKnowledgeMapper knowledgeMapper;

    @Autowired
    private AiConfig aiConfig;

    /** 疾病 -> 知识条目列表 */
    private Map<String, List<MedicalKnowledge>> knowledgeIndex = new HashMap<>();

    @PostConstruct
    public void init() {
        reload();
    }

    /** 允许 Service 层手动触发知识库热更新 */
    public void reload() {
        List<MedicalKnowledge> all = knowledgeMapper.selectAll();
        knowledgeIndex = all.stream().collect(Collectors.groupingBy(MedicalKnowledge::getDiseaseName));
        log.info("医学知识库加载完成: {} 条映射, {} 种疾病", all.size(), knowledgeIndex.size());
    }

    @Override
    public DiagnosisResponse diagnose(DiagnosisRequest request, Map<Long, String> symptomNames) {
        Set<Long> userSymptoms = new HashSet<>(request.getSymptomIds());
        List<DiagnosisResponse.Candidate> candidates = new ArrayList<>();

        for (Map.Entry<String, List<MedicalKnowledge>> e : knowledgeIndex.entrySet()) {
            String disease = e.getKey();
            List<MedicalKnowledge> rules = e.getValue();

            double totalWeight = rules.stream().mapToDouble(k -> k.getWeight().doubleValue()).sum();
            double matchedWeight = 0;
            boolean requiredMissed = false;
            List<String> hits = new ArrayList<>();

            for (MedicalKnowledge k : rules) {
                if (userSymptoms.contains(k.getSymptomId())) {
                    matchedWeight += k.getWeight().doubleValue();
                    hits.add(symptomNames.getOrDefault(k.getSymptomId(), String.valueOf(k.getSymptomId())));
                } else if (k.getRequired() != null && k.getRequired() == 1) {
                    requiredMissed = true;
                }
            }

            double score = totalWeight == 0 ? 0 : matchedWeight / totalWeight;
            if (requiredMissed) score *= 0.4;
            if (score < aiConfig.getConfidenceThreshold()) continue;

            candidates.add(new DiagnosisResponse.Candidate(
                    disease,
                    BigDecimal.valueOf(score).setScale(3, RoundingMode.HALF_UP),
                    hits
            ));
        }

        candidates.sort(Comparator.comparing(DiagnosisResponse.Candidate::getScore).reversed());
        List<DiagnosisResponse.Candidate> top = candidates.stream().limit(topK()).toList();

        DiagnosisResponse resp = new DiagnosisResponse();
        resp.setCandidates(top);
        if (!top.isEmpty()) {
            DiagnosisResponse.Candidate first = top.get(0);
            resp.setTopDisease(first.getDisease());
            resp.setConfidence(first.getScore());
            resp.setSeverity(computeSeverity(first.getDisease(), symptomNames));
            resp.setSuggestion(buildSuggestion(first.getDisease(), first.getScore()));
        } else {
            resp.setTopDisease("UNKNOWN");
            resp.setConfidence(BigDecimal.ZERO);
            resp.setSeverity(1);
            resp.setSuggestion("症状不足以匹配已知疾病，建议补充信息或线下就医。");
        }
        return resp;
    }

    private Integer computeSeverity(String disease, Map<Long, String> symptomNames) {
        // 简易规则：心血管/呼吸类默认严重度较高
        if (disease.contains("心") || disease.contains("肺")) return 3;
        if (disease.contains("炎") || disease.contains("感冒")) return 2;
        return 1;
    }

    private String buildSuggestion(String disease, BigDecimal score) {
        double s = score.doubleValue();
        if (s >= 0.8) {
            return "高度疑似【" + disease + "】，建议尽快前往对应科室就诊，并进行相关检查确认。";
        } else if (s >= 0.6) {
            return "可能为【" + disease + "】，建议观察症状变化，如加重请及时就医。";
        } else {
            return "存在【" + disease + "】的可能性，建议结合线下医生问诊结果综合判断。";
        }
    }

    @Override
    public String engineType() {
        return "RULE";
    }
}

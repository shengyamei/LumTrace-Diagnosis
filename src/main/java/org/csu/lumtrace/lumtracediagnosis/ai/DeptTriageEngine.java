package org.csu.lumtrace.lumtracediagnosis.ai;

import jakarta.annotation.PostConstruct;
import org.csu.lumtrace.lumtracediagnosis.entity.DeptRule;
import org.csu.lumtrace.lumtracediagnosis.mapper.DeptRuleMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 科室分诊规则匹配原型（架构说明书 2.4 规则引擎层，成员C / W1）。
 * <p>
 * 匹配策略：对用户主诉做规则库全量关键词包含判断，
 * 同一科室命中的关键词长度累加为得分（长关键词优先，避免「疼」压过「心口疼」），
 * 取最高分科室返回；全不命中返回 {@code null}，交由大模型兜底。
 * <p>
 * 匹配算法与数据源解耦：{@link #match(String, List)} 为纯函数，可脱离数据库单测。
 */
@Component
public class DeptTriageEngine {
    private static final Logger log = LoggerFactory.getLogger(DeptTriageEngine.class);

    @Autowired
    private DeptRuleMapper deptRuleMapper;

    /** 分诊规则缓存 */
    private List<DeptRule> rules = List.of();

    @PostConstruct
    public void init() {
        reload();
    }

    /** 规则库热更新；加载失败不影响启动，走大模型兜底 */
    public void reload() {
        try {
            List<DeptRule> loaded = deptRuleMapper.selectAll();
            this.rules = loaded == null ? List.of() : List.copyOf(loaded);
            log.info("分诊规则库加载完成: {} 条规则, {} 个科室",
                    rules.size(), rules.stream().map(DeptRule::getDeptName).distinct().count());
        } catch (Exception e) {
            log.error("分诊规则库加载失败，分诊将全部走大模型兜底", e);
        }
    }

    /** 用当前缓存的规则库做匹配 */
    public TriageResult triage(String text) {
        return match(text, rules);
    }

    /**
     * 纯函数匹配，便于单元测试与规则库离线校验。
     *
     * @param text  用户主诉文本
     * @param rules 规则列表
     * @return 命中结果；无任何关键词命中时返回 null（调用方转向大模型兜底）
     */
    public static TriageResult match(String text, List<DeptRule> rules) {
        if (text == null || text.isBlank() || rules == null || rules.isEmpty()) {
            return null;
        }

        // 科室 -> 命中的关键词
        Map<String, List<String>> deptHits = new LinkedHashMap<>();
        for (DeptRule rule : rules) {
            if (rule == null || rule.getKeyword() == null || rule.getKeyword().isBlank()) continue;
            if (text.contains(rule.getKeyword())) {
                deptHits.computeIfAbsent(rule.getDeptName(), k -> new ArrayList<>()).add(rule.getKeyword());
            }
        }
        if (deptHits.isEmpty()) {
            return null;
        }

        String bestDept = null;
        List<String> bestHits = List.of();
        int bestScore = -1;
        for (Map.Entry<String, List<String>> entry : deptHits.entrySet()) {
            int score = entry.getValue().stream().mapToInt(String::length).sum();
            if (score > bestScore) {
                bestScore = score;
                bestDept = entry.getKey();
                bestHits = entry.getValue();
            }
        }

        return new TriageResult(bestDept, findTip(bestDept, rules), List.copyOf(bestHits), bestScore);
    }

    private static String findTip(String deptName, List<DeptRule> rules) {
        for (DeptRule rule : rules) {
            if (deptName != null && deptName.equals(rule.getDeptName())
                    && rule.getTip() != null && !rule.getTip().isBlank()) {
                return rule.getTip();
            }
        }
        return null;
    }

    /**
     * @param deptName    推荐科室
     * @param tip         就医提示
     * @param hitKeywords 命中的关键词
     * @param score       匹配得分（命中关键词长度累加）
     */
    public record TriageResult(String deptName, String tip, List<String> hitKeywords, int score) {
    }
}

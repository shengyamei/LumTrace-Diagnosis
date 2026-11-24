package org.csu.lumtrace.lumtracediagnosis.service.impl;

import org.csu.lumtrace.lumtracediagnosis.config.AiConfig;
import org.csu.lumtrace.lumtracediagnosis.dto.response.DrugFoodResponse;
import org.csu.lumtrace.lumtracediagnosis.entity.DrugFoodRule;
import org.csu.lumtrace.lumtracediagnosis.mapper.DrugFoodRuleMapper;
import org.csu.lumtrace.lumtracediagnosis.service.DrugFoodService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用药饮食禁忌查询服务：本地禁忌库优先 → 大模型兜底
 */
@Service
public class DrugFoodServiceImpl implements DrugFoodService {
    private static final Logger log = LoggerFactory.getLogger(DrugFoodServiceImpl.class);

    @Autowired
    private DrugFoodRuleMapper drugFoodRuleMapper;

    @Autowired
    private AiConfig aiConfig;

    @Override
    public DrugFoodResponse queryByDrugName(String drugName) {
        // 1. 优先匹配本地禁忌库
        List<DrugFoodRule> rules = drugFoodRuleMapper.selectByDrugName(drugName);
        if (!rules.isEmpty()) {
            // 精确匹配优先
            DrugFoodRule best = rules.stream()
                    .filter(r -> r.getDrugName().equals(drugName))
                    .findFirst()
                    .orElse(rules.get(0));
            log.info("禁忌规则命中: drug={}", best.getDrugName());
            return DrugFoodResponse.ofRule(best.getDrugName(), best.getForbidFood(),
                    best.getSuggestFood(), best.getTip());
        }

        // 2. 判断是否启用大模型
        if (aiConfig.isOfflineMode() || !"LLM".equalsIgnoreCase(aiConfig.getEngine())) {
            return DrugFoodResponse.ofRule(drugName, null, null,
                    "暂未收录该药品禁忌信息，建议咨询药师或查看药品说明书。");
        }

        // 3. 调用大模型补充查询
        try {
            return callLlmForDrugFood(drugName);
        } catch (Exception e) {
            log.error("大模型禁忌查询失败: {}", e.getMessage());
            return DrugFoodResponse.ofRule(drugName, null, null,
                    "查询失败，建议咨询药师或查看药品说明书。");
        }
    }

    /**
     * 调用大模型查询药品饮食禁忌
     */
    private DrugFoodResponse callLlmForDrugFood(String drugName) {
        // TODO: 接入真实LLM API
        log.info("调用LLM禁忌查询: drug={}", drugName);

        // 占位实现：返回通用建议
        return DrugFoodResponse.ofLlm(drugName,
                "暂无精确数据",
                "清淡饮食为主，避免辛辣油腻",
                "建议服药期间避免酒精及刺激性食物，具体请参照药品说明书或咨询药师。");
    }
}

package org.csu.lumtrace.lumtracediagnosis.dto.response;

import lombok.Data;

import java.io.Serializable;

/**
 * 用药饮食禁忌查询响应
 */
@Data
public class DrugFoodResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 药品名称 */
    private String drugName;
    /** 禁忌食物 */
    private String forbidFood;
    /** 推荐饮食 */
    private String suggestFood;
    /** 提示说明 */
    private String tip;
    /** 数据来源：RULE / LLM */
    private String source;
    /** AI整理标注 */
    private String disclaimer;

    public static DrugFoodResponse ofRule(String drug, String forbid, String suggest, String tip) {
        DrugFoodResponse r = new DrugFoodResponse();
        r.setDrugName(drug);
        r.setForbidFood(forbid);
        r.setSuggestFood(suggest);
        r.setTip(tip);
        r.setSource("RULE");
        return r;
    }

    public static DrugFoodResponse ofLlm(String drug, String forbid, String suggest, String tip) {
        DrugFoodResponse r = new DrugFoodResponse();
        r.setDrugName(drug);
        r.setForbidFood(forbid);
        r.setSuggestFood(suggest);
        r.setTip(tip);
        r.setSource("LLM");
        r.setDisclaimer("AI整理，仅供参考");
        return r;
    }
}

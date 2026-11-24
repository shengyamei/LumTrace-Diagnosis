package org.csu.lumtrace.lumtracediagnosis.dto.response;

import lombok.Data;

import java.io.Serializable;

/**
 * AI导诊分诊响应
 */
@Data
public class TriageResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 推荐科室 */
    private String deptName;
    /** 就医提示 */
    private String tip;
    /** 匹配方式：RULE / LLM */
    private String matchType;
    /** 是否命中规则库 */
    private boolean ruleHit;
    /** 挂号引导链接（模拟） */
    private String registerUrl;

    public static TriageResponse ofRule(String dept, String tip) {
        TriageResponse r = new TriageResponse();
        r.setDeptName(dept);
        r.setTip(tip);
        r.setMatchType("RULE");
        r.setRuleHit(true);
        r.setRegisterUrl("/mock-register?dept=" + dept);
        return r;
    }

    public static TriageResponse ofLlm(String dept, String tip) {
        TriageResponse r = new TriageResponse();
        r.setDeptName(dept);
        r.setTip(tip);
        r.setMatchType("LLM");
        r.setRuleHit(false);
        r.setRegisterUrl("/mock-register?dept=" + dept);
        return r;
    }
}

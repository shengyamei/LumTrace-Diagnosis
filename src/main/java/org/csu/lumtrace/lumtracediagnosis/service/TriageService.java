package org.csu.lumtrace.lumtracediagnosis.service;

import org.csu.lumtrace.lumtracediagnosis.dto.response.TriageResponse;

/**
 * AI导诊分诊服务
 */
public interface TriageService {
    /**
     * 根据症状描述文本进行分诊
     * @param symptomText 用户输入的症状描述
     * @return 分诊结果（推荐科室+就医提示）
     */
    TriageResponse triage(String symptomText);
}

package org.csu.lumtrace.lumtracediagnosis.dto.response;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * AI 诊断响应
 */
@Data
public class DiagnosisResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 记录ID（保存后返回） */
    private Long recordId;
    /** 最可能疾病 */
    private String topDisease;
    /** 置信度 */
    private BigDecimal confidence;
    /** 严重度：1-轻 2-中 3-重 */
    private Integer severity;
    /** 建议 */
    private String suggestion;
    /** 推荐就诊科室（分诊输出，W2 分诊链路接入后填充） */
    private String deptName;
    /** 分诊就医提示 */
    private String tip;
    /** 免责声明（架构说明书 1.1 固定文案，所有AI输出必带） */
    private String disclaimer;
    /** 候选疾病列表（按分数排序） */
    private List<Candidate> candidates;

    @Data
    public static class Candidate implements Serializable {
        private String disease;
        private BigDecimal score;
        private List<String> hitSymptoms;

        public Candidate(String disease, BigDecimal score, List<String> hitSymptoms) {
            this.disease = disease;
            this.score = score;
            this.hitSymptoms = hitSymptoms;
        }
    }
}

package org.csu.lumtrace.lumtracediagnosis.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * AI 引擎相关配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "lumtrace.ai")
public class AiConfig {
    /** 引擎类型：RULE / LLM */
    private String engine = "RULE";
    /** 断网降级开关：true=仅走规则库，不调大模型 */
    private boolean offlineMode = false;
    /** 输出候选结果的最小置信度阈值 */
    private Double confidenceThreshold = 0.35;
    private Llm llm = new Llm();

    @Data
    public static class Llm {
        private String baseUrl;
        private String apiKey;
        private String model;
        private Integer timeoutMs = 15000;
        /** 分诊约束系统提示词 */
        private String systemPrompt = "你是分诊助手，禁止诊断疾病，禁止给出治疗方案，仅根据用户描述症状推荐就诊科室并给出简单就医提示，最后提醒本内容仅供参考，请以医生面诊为准。";
    }
}

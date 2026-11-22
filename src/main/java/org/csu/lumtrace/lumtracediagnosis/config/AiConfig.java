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
    /** 离线降级开关：true 时完全不调用大模型，仅本地规则库运行（现场断网演示） */
    private Boolean offlineMode = false;
    /** 输出候选疾病的最小置信度阈值 */
    private Double confidenceThreshold = 0.35;
    private Llm llm = new Llm();

    @Data
    public static class Llm {
        private String baseUrl;
        private String apiKey;
        private String model;
        private Integer timeoutMs = 15000;
    }
}

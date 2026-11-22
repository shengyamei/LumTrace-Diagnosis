package org.csu.lumtrace.lumtracediagnosis.ai.llm;

import org.csu.lumtrace.lumtracediagnosis.config.AiConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 大模型调用封装（成员C / W1：接入 + 冒烟）。
 * <p>
 * 只依赖 spring-web 自带的 {@link RestClient}，不引第三方 SDK，
 * 换供应商（通义千问 / 文心一言 / 本地 Qwen）只改 application.yaml 的 base-url 与 model。
 * <p>
 * 返回体按 Map 反序列化，避免手写 Jackson 对象映射（SpringBoot 4 已迁移 Jackson 3 包名）。
 */
@Component
public class LlmClient {
    private static final Logger log = LoggerFactory.getLogger(LlmClient.class);

    private final AiConfig aiConfig;
    private final RestClient restClient;

    public LlmClient(AiConfig aiConfig) {
        this.aiConfig = aiConfig;
        AiConfig.Llm llm = aiConfig.getLlm();
        int timeoutMs = llm.getTimeoutMs() == null ? 15000 : llm.getTimeoutMs();
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeoutMs);
        factory.setReadTimeout(timeoutMs);
        this.restClient = RestClient.builder().requestFactory(factory).build();
    }

    /** 是否具备真实调用条件：未开离线模式 且 配置了 apiKey */
    public boolean isAvailable() {
        AiConfig.Llm llm = aiConfig.getLlm();
        boolean offline = Boolean.TRUE.equals(aiConfig.getOfflineMode());
        boolean hasKey = llm.getApiKey() != null && !llm.getApiKey().isBlank();
        return !offline && hasKey;
    }

    /** 当前是否处于离线降级模式 */
    public boolean isOffline() {
        return Boolean.TRUE.equals(aiConfig.getOfflineMode());
    }

    /**
     * 调用 OpenAI 兼容的 /chat/completions 接口。
     *
     * @return 模型返回的 content 原文
     * @throws LlmUnavailableException 未配置 Key / 离线模式 / 调用与解析失败
     */
    @SuppressWarnings("unchecked")
    public String chat(String userPrompt) {
        AiConfig.Llm llm = aiConfig.getLlm();
        if (isOffline()) {
            throw new LlmUnavailableException("离线模式已开启，不调用大模型");
        }
        if (llm.getApiKey() == null || llm.getApiKey().isBlank()) {
            throw new LlmUnavailableException("大模型 apiKey 未配置");
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", llm.getModel());
        body.put("temperature", 0.2);
        body.put("messages", List.of(
                Map.of("role", "system", "content", PromptTemplates.SYSTEM_PROMPT),
                Map.of("role", "user", "content", userPrompt)
        ));

        long start = System.currentTimeMillis();
        Map<String, Object> resp;
        try {
            resp = restClient.post()
                    .uri(buildChatUrl(llm.getBaseUrl()))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + llm.getApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(Map.class);
        } catch (Exception e) {
            throw new LlmUnavailableException("大模型调用失败: " + e.getMessage(), e);
        }

        String content = extractContent(resp);
        if (content == null || content.isBlank()) {
            throw new LlmUnavailableException("大模型返回内容为空");
        }
        log.info("大模型调用成功: model={}, 耗时={}ms, 返回长度={}",
                llm.getModel(), System.currentTimeMillis() - start, content.length());
        return content;
    }

    private static String buildChatUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new LlmUnavailableException("大模型 base-url 未配置");
        }
        String base = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        return base + "/chat/completions";
    }

    /** 兼容 OpenAI 格式：choices[0].message.content */
    @SuppressWarnings("unchecked")
    private static String extractContent(Map<String, Object> resp) {
        if (resp == null) return null;
        Object choicesObj = resp.get("choices");
        if (!(choicesObj instanceof List<?> choices) || choices.isEmpty()) return null;
        Object first = choices.get(0);
        if (!(first instanceof Map<?, ?> choice)) return null;
        Object messageObj = choice.get("message");
        if (!(messageObj instanceof Map<?, ?> message)) return null;
        Object content = message.get("content");
        return content == null ? null : String.valueOf(content);
    }
}

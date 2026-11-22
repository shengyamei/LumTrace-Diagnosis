package org.csu.lumtrace.lumtracediagnosis.ai.llm;

/**
 * 大模型不可用（未配置 Key、离线模式、调用失败、返回不可解析）。
 * <p>
 * 抛出后由 {@code DiagnosisEngineRouter} 统一降级到本地规则引擎，
 * 保证任何情况下用户都能拿到结果，不会看到报错页。
 */
public class LlmUnavailableException extends RuntimeException {

    public LlmUnavailableException(String message) {
        super(message);
    }

    public LlmUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}

package org.csu.lumtrace.lumtracediagnosis.filter;

import org.csu.lumtrace.lumtracediagnosis.common.constant.AppConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 敏感词过滤器单元测试。
 * <p>
 * 不启动 Spring 容器，直接手动构造并加载真实词库，
 * 保证「词库改动能被这个测试发现」。
 */
class SensitiveWordFilterTest {

    private SensitiveWordFilter filter;

    @BeforeEach
    void setUp() {
        filter = new SensitiveWordFilter(new DefaultResourceLoader());
        filter.init();
    }

    @Test
    @DisplayName("词库加载成功且两类词条都不为空")
    void shouldLoadWordList() {
        assertTrue(filter.blockSize() > 0, "拦截词库不应为空");
        assertTrue(filter.rewriteSize() > 0, "改写词库不应为空");
    }

    @Test
    @DisplayName("命中确诊/用药类表述时整句拦截并替换为安全话术")
    void shouldBlockDiagnosisAndMedication() {
        String raw = "您已确诊为急性咽炎，建议服用头孢克肟，一次两片。";
        SensitiveWordFilter.FilterResult result = filter.check(raw);

        assertTrue(result.blocked(), "确诊+用药表述必须触发拦截");
        assertEquals(AppConstants.SAFE_SUGGESTION, result.safeText());
        assertTrue(result.hitWords().contains("确诊"));
        assertTrue(result.hitWords().contains("服用"));
    }

    @Test
    @DisplayName("命中绝对化表述时软化为可能类措辞，不整句丢弃")
    void shouldRewriteAbsoluteWords() {
        String raw = "多喝热水，这种情况一定没问题的。";
        SensitiveWordFilter.FilterResult result = filter.check(raw);

        assertFalse(result.blocked(), "绝对化表述只做软化，不拦截");
        assertEquals("多喝热水，这种情况可能没问题的。", result.safeText());
        assertTrue(result.hitWords().contains("一定"));
    }

    @Test
    @DisplayName("正常就医提示不被误杀")
    void shouldKeepCompliantText() {
        String raw = "建议前往耳鼻喉科就诊，注意休息、多饮水，如持续发热请及时复诊。";
        SensitiveWordFilter.FilterResult result = filter.check(raw);

        assertFalse(result.blocked());
        assertEquals(raw, result.safeText());
        assertTrue(result.hitWords().isEmpty());
    }

    @Test
    @DisplayName("空文本原样返回，不抛异常")
    void shouldHandleBlankText() {
        SensitiveWordFilter.FilterResult blank = filter.check("   ");
        assertFalse(blank.blocked());
        assertEquals("   ", blank.safeText());

        SensitiveWordFilter.FilterResult nullText = filter.check(null);
        assertFalse(nullText.blocked());
        assertNull(nullText.safeText());
    }

    @Test
    @DisplayName("命中次数被记录，可用于答辩抽检")
    void shouldRecordHitCounter() {
        filter.check("这个病是感冒，建议口服感冒灵。");
        filter.check("你这个病不严重。");

        assertTrue(filter.hitSnapshot().values().stream().mapToInt(Integer::intValue).sum() > 0);
    }
}

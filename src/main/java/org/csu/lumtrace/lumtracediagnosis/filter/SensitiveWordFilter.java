package org.csu.lumtrace.lumtracediagnosis.filter;

import jakarta.annotation.PostConstruct;
import org.csu.lumtrace.lumtracediagnosis.common.constant.AppConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 医疗输出敏感词过滤器（架构说明书 2.4）。
 * <p>
 * 双层策略：
 * <ul>
 *   <li><b>BLOCK</b>：命中确诊结论 / 用药方案 / 替代就医类表述，整句替换为标准就医提示；</li>
 *   <li><b>REWRITE</b>：命中绝对化表述，词级软化为「可能」类措辞。</li>
 * </ul>
 * 词库来源 {@code classpath:filter/sensitive-words.txt}，命中明细全部记日志并计数，
 * 用于答辩抽检与回归比对。
 */
@Component
public class SensitiveWordFilter {
    private static final Logger log = LoggerFactory.getLogger(SensitiveWordFilter.class);
    private static final String DEFAULT_LOCATION = "classpath:filter/sensitive-words.txt";

    private static final int MODE_NONE = 0;
    private static final int MODE_BLOCK = 1;
    private static final int MODE_REWRITE = 2;

    private final ResourceLoader resourceLoader;

    private List<String> blockWords = List.of();
    private Map<String, String> rewriteWords = Map.of();

    /** 词条 -> 命中次数 */
    private final Map<String, AtomicInteger> hitCounter = new ConcurrentHashMap<>();

    public SensitiveWordFilter(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    public void init() {
        load(DEFAULT_LOCATION);
    }

    /** 加载词库；启动加载与运行时热更新共用 */
    public void load(String location) {
        List<String> blocks = new ArrayList<>();
        Map<String, String> rewrites = new LinkedHashMap<>();
        Resource resource = resourceLoader.getResource(location);
        int mode = MODE_NONE;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String s = line.trim();
                if (s.isEmpty()) continue;
                if (s.startsWith("#")) {
                    if (s.contains("[BLOCK]")) mode = MODE_BLOCK;
                    else if (s.contains("[REWRITE]")) mode = MODE_REWRITE;
                    continue;
                }
                if (mode == MODE_BLOCK) {
                    blocks.add(s);
                } else if (mode == MODE_REWRITE) {
                    String[] pair = s.split("=>", 2);
                    if (pair.length == 2 && !pair[0].isBlank()) {
                        rewrites.put(pair[0].trim(), pair[1].trim());
                    }
                }
            }
        } catch (IOException e) {
            log.error("敏感词库加载失败: {}", location, e);
        }
        this.blockWords = List.copyOf(blocks);
        this.rewriteWords = Collections.unmodifiableMap(new LinkedHashMap<>(rewrites));
        log.info("敏感词库加载完成: 拦截词 {} 个, 改写词 {} 个, 来源 {}",
                blockWords.size(), rewriteWords.size(), location);
    }

    /**
     * 检查一段输出文本。
     *
     * @return 命中详情；blocked=true 时 safeText 已替换为标准就医提示
     */
    public FilterResult check(String text) {
        if (text == null || text.isBlank()) {
            return new FilterResult(false, text, List.of());
        }

        List<String> hits = new ArrayList<>();
        for (String word : blockWords) {
            if (text.contains(word)) hits.add(word);
        }
        if (!hits.isEmpty()) {
            recordHits(hits);
            log.warn("敏感词拦截: 命中={} 原文=[{}]", hits, text);
            return new FilterResult(true, AppConstants.SAFE_SUGGESTION, hits);
        }

        String safe = text;
        for (Map.Entry<String, String> e : rewriteWords.entrySet()) {
            if (safe.contains(e.getKey())) {
                safe = safe.replace(e.getKey(), e.getValue());
                hits.add(e.getKey());
            }
        }
        if (!hits.isEmpty()) {
            recordHits(hits);
            log.info("敏感词改写: 命中={} 改写后=[{}]", hits, safe);
        }
        return new FilterResult(false, safe, hits);
    }

    /** 便捷入口：只取过滤后的安全文本 */
    public String sanitize(String text) {
        return check(text).safeText();
    }

    /** 命中次数快照，供答辩抽检与回归比对 */
    public Map<String, Integer> hitSnapshot() {
        Map<String, Integer> snapshot = new LinkedHashMap<>();
        hitCounter.forEach((k, v) -> snapshot.put(k, v.get()));
        return snapshot;
    }

    public int blockSize() {
        return blockWords.size();
    }

    public int rewriteSize() {
        return rewriteWords.size();
    }

    private void recordHits(List<String> hits) {
        for (String w : hits) {
            hitCounter.computeIfAbsent(w, k -> new AtomicInteger()).incrementAndGet();
        }
    }

    /**
     * @param blocked  是否触发拦截档
     * @param safeText 可直接对外输出的文本
     * @param hitWords 命中的词条
     */
    public record FilterResult(boolean blocked, String safeText, List<String> hitWords) {
    }
}

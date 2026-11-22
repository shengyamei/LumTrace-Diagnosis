package org.csu.lumtrace.lumtracediagnosis.controller;

import org.csu.lumtrace.lumtracediagnosis.ai.DeptTriageEngine;
import org.csu.lumtrace.lumtracediagnosis.ai.llm.LlmClient;
import org.csu.lumtrace.lumtracediagnosis.ai.llm.PromptTemplates;
import org.csu.lumtrace.lumtracediagnosis.config.AiConfig;
import org.csu.lumtrace.lumtracediagnosis.dto.response.Result;
import org.csu.lumtrace.lumtracediagnosis.filter.SensitiveWordFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 层调试与验收端点（成员C / W1）。
 * <p>
 * 三个入口都受登录鉴权拦截（走 /ai/**，需先调 /user/login 拿 token）：
 * <ul>
 *   <li>GET  /ai/smoke       大模型连通性冒烟，验证 Key、耗时与返回内容</li>
 *   <li>GET  /ai/dept-triage 分诊规则匹配原型联调</li>
 *   <li>POST /ai/reload      规则库与敏感词库热更新，改表改词后免重启</li>
 * </ul>
 */
@RestController
@RequestMapping("/ai")
public class AiSmokeController {

    @Autowired
    private LlmClient llmClient;

    @Autowired
    private DeptTriageEngine deptTriageEngine;

    @Autowired
    private SensitiveWordFilter sensitiveWordFilter;

    @Autowired
    private AiConfig aiConfig;

    /** 大模型冒烟调用：成功返回原文 + 过滤后文本，失败返回原因（用于验收留证） */
    @GetMapping("/smoke")
    public Result<Map<String, Object>> smoke(@RequestParam("text") String text) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("engine", aiConfig.getEngine());
        data.put("offlineMode", llmClient.isOffline());
        data.put("llmAvailable", llmClient.isAvailable());

        long start = System.currentTimeMillis();
        try {
            String raw = llmClient.chat(PromptTemplates.buildUserPrompt(text, List.of()));
            data.put("success", true);
            data.put("raw", raw);
            data.put("filtered", sensitiveWordFilter.sanitize(raw));
        } catch (Exception e) {
            data.put("success", false);
            data.put("error", e.getMessage());
        }
        data.put("costMs", System.currentTimeMillis() - start);
        return Result.success(data);
    }

    /** 分诊规则匹配原型：命中返回科室与提示，未命中提示转大模型兜底 */
    @GetMapping("/dept-triage")
    public Result<Map<String, Object>> deptTriage(@RequestParam("text") String text) {
        DeptTriageEngine.TriageResult result = deptTriageEngine.triage(text);
        Map<String, Object> data = new LinkedHashMap<>();
        if (result == null) {
            data.put("hit", false);
            data.put("message", "规则库未命中，转大模型兜底");
        } else {
            data.put("hit", true);
            data.put("deptName", result.deptName());
            data.put("tip", result.tip());
            data.put("hitKeywords", result.hitKeywords());
            data.put("score", result.score());
        }
        return Result.success(data);
    }

    /** 规则库与敏感词库热更新 */
    @PostMapping("/reload")
    public Result<Map<String, Object>> reload() {
        deptTriageEngine.reload();
        sensitiveWordFilter.load("classpath:filter/sensitive-words.txt");

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("deptRules", "reloaded");
        data.put("blockWords", sensitiveWordFilter.blockSize());
        data.put("rewriteWords", sensitiveWordFilter.rewriteSize());
        return Result.success(data);
    }
}

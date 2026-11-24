package org.csu.lumtrace.lumtracediagnosis.controller;

import org.csu.lumtrace.lumtracediagnosis.dto.request.TriageRequest;
import org.csu.lumtrace.lumtracediagnosis.dto.response.Result;
import org.csu.lumtrace.lumtracediagnosis.dto.response.TriageResponse;
import org.csu.lumtrace.lumtracediagnosis.service.TriageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI导诊分诊接口
 * 职责：接收请求 → 调用 TriageService → 返回推荐科室与就医提示
 * 注意：规则引擎实现、大模型调用、敏感词过滤由成员C负责，此处仅为接口层
 */
@RestController
@RequestMapping("/triage")
public class TriageController {

    @Autowired
    private TriageService triageService;

    /**
     * 提交症状描述，获取推荐科室
     * POST /triage/analyze
     */
    @PostMapping("/analyze")
    public Result<TriageResponse> analyze(@Valid @RequestBody TriageRequest req) {
        TriageResponse response = triageService.triage(req.getSymptomText());
        return Result.success(response);
    }
}

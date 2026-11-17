package org.csu.lumtrace.lumtracediagnosis.controller;

import org.csu.lumtrace.lumtracediagnosis.common.util.UserContext;
import org.csu.lumtrace.lumtracediagnosis.dto.request.DiagnosisRequest;
import org.csu.lumtrace.lumtracediagnosis.dto.response.DiagnosisResponse;
import org.csu.lumtrace.lumtracediagnosis.dto.response.Result;
import org.csu.lumtrace.lumtracediagnosis.entity.DiagnosisRecord;
import org.csu.lumtrace.lumtracediagnosis.service.DiagnosisService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/diagnosis")
public class DiagnosisController {

    @Autowired
    private DiagnosisService diagnosisService;

    /** 提交AI诊断 */
    @PostMapping("/submit")
    public Result<DiagnosisResponse> submit(@Valid @RequestBody DiagnosisRequest req) {
        Long userId = UserContext.getUserId();
        return Result.success(diagnosisService.diagnose(userId, req));
    }

    /** 我的诊断历史 */
    @GetMapping("/history")
    public Result<List<DiagnosisRecord>> history() {
        return Result.success(diagnosisService.history(UserContext.getUserId()));
    }

    @GetMapping("/{id}")
    public Result<DiagnosisRecord> detail(@PathVariable Long id) {
        return Result.success(diagnosisService.getById(id));
    }
}

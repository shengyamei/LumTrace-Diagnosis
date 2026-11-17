package org.csu.lumtrace.lumtracediagnosis.controller;

import org.csu.lumtrace.lumtracediagnosis.common.util.UserContext;
import org.csu.lumtrace.lumtracediagnosis.dto.response.Result;
import org.csu.lumtrace.lumtracediagnosis.entity.DiagnosisReport;
import org.csu.lumtrace.lumtracediagnosis.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/report")
public class ReportController {

    @Autowired
    private ReportService reportService;

    /** 我的报告列表 */
    @GetMapping("/my")
    public Result<List<DiagnosisReport>> my() {
        return Result.success(reportService.listByUser(UserContext.getUserId()));
    }

    /** 通过报告编号查询 */
    @GetMapping("/no/{reportNo}")
    public Result<DiagnosisReport> byNo(@PathVariable String reportNo) {
        return Result.success(reportService.getByNo(reportNo));
    }

    /** 医生复核 */
    @PostMapping("/review/{reportId}")
    public Result<DiagnosisReport> review(@PathVariable Long reportId, @RequestBody Map<String, String> body) {
        Long doctorUserId = UserContext.getUserId();
        return Result.success(reportService.review(doctorUserId, reportId, body.get("review")));
    }
}

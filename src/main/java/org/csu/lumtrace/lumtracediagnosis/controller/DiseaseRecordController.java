package org.csu.lumtrace.lumtracediagnosis.controller;

import org.csu.lumtrace.lumtracediagnosis.common.util.UserContext;
import org.csu.lumtrace.lumtracediagnosis.dto.response.Result;
import org.csu.lumtrace.lumtracediagnosis.entity.DiseaseRecord;
import org.csu.lumtrace.lumtracediagnosis.service.DiseaseRecordService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 病程记录接口（队长/成员A 负责）
 * 增删改查 + 时间线倒序 + 一键导出病程文本
 */
@RestController
@RequestMapping("/record")
public class DiseaseRecordController {

    @Autowired
    private DiseaseRecordService recordService;

    /**
     * 新增病程记录
     * POST /record
     */
    @PostMapping
    public Result<DiseaseRecord> create(@Valid @RequestBody DiseaseRecord record) {
        Long userId = UserContext.getUserId();
        return Result.success(recordService.create(userId, record));
    }

    /**
     * 修改病程记录
     * PUT /record/{id}
     */
    @PutMapping("/{id}")
    public Result<DiseaseRecord> update(@PathVariable Long id, @RequestBody DiseaseRecord record) {
        Long userId = UserContext.getUserId();
        record.setId(id);
        return Result.success(recordService.update(userId, record));
    }

    /**
     * 删除病程记录
     * DELETE /record/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        recordService.delete(userId, id);
        return Result.success();
    }

    /**
     * 查询病程时间线（按 record_time 倒序）
     * GET /record/timeline
     */
    @GetMapping("/timeline")
    public Result<List<DiseaseRecord>> timeline() {
        Long userId = UserContext.getUserId();
        return Result.success(recordService.timeline(userId));
    }

    /**
     * 一键导出病程文本（纯文本，可直接复制给医生）
     * GET /record/export
     */
    @GetMapping("/export")
    public ResponseEntity<String> export() {
        Long userId = UserContext.getUserId();
        String text = recordService.exportText(userId);

        String fileName = "disease_record_"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                + ".txt";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "plain", StandardCharsets.UTF_8));
        headers.setContentDispositionFormData("attachment", fileName);

        return ResponseEntity.ok()
                .headers(headers)
                .body(text);
    }
}

package org.csu.lumtrace.lumtracediagnosis.controller;

import org.csu.lumtrace.lumtracediagnosis.dto.response.Result;
import org.csu.lumtrace.lumtracediagnosis.entity.Symptom;
import org.csu.lumtrace.lumtracediagnosis.service.SymptomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/symptom")
public class SymptomController {

    @Autowired
    private SymptomService symptomService;

    /** 全量症状（免登录） */
    @GetMapping("/list")
    public Result<List<Symptom>> list() {
        return Result.success(symptomService.listAll());
    }

    @GetMapping("/category")
    public Result<List<Symptom>> byCategory(@RequestParam String category) {
        return Result.success(symptomService.listByCategory(category));
    }

    @GetMapping("/search")
    public Result<List<Symptom>> search(@RequestParam(required = false) String keyword) {
        return Result.success(symptomService.search(keyword));
    }
}

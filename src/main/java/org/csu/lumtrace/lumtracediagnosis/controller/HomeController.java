package org.csu.lumtrace.lumtracediagnosis.controller;

import org.csu.lumtrace.lumtracediagnosis.dto.response.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 健康检查/首页
 */
@RestController
public class HomeController {

    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        return Result.success(Map.of(
                "app", "LumTrace-Diagnosis",
                "status", "UP",
                "version", "0.0.1-SNAPSHOT"
        ));
    }
}

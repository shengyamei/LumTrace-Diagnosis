package org.csu.lumtrace.lumtracediagnosis.controller;

import org.csu.lumtrace.lumtracediagnosis.dto.response.DrugFoodResponse;
import org.csu.lumtrace.lumtracediagnosis.dto.response.Result;
import org.csu.lumtrace.lumtracediagnosis.service.DrugFoodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用药饮食禁忌查询接口
 * 职责：接收药名 → 调用 DrugFoodService → 返回禁忌/推荐饮食
 * 注意：禁忌知识库内容由成员D编纂，大模型兜底由成员C实现，此处仅为接口层
 */
@RestController
@RequestMapping("/drug-food")
public class DrugFoodController {

    @Autowired
    private DrugFoodService drugFoodService;

    /**
     * 根据药品名称查询饮食禁忌
     * GET /drug-food/query?drugName=头孢克肟
     */
    @GetMapping("/query")
    public Result<DrugFoodResponse> query(@RequestParam String drugName) {
        return Result.success(drugFoodService.queryByDrugName(drugName));
    }
}

package org.csu.lumtrace.lumtracediagnosis.service;

import org.csu.lumtrace.lumtracediagnosis.dto.response.DrugFoodResponse;

/**
 * 用药饮食禁忌查询服务
 */
public interface DrugFoodService {
    /**
     * 根据药品名称查询饮食禁忌
     * @param drugName 药品名称
     * @return 禁忌信息
     */
    DrugFoodResponse queryByDrugName(String drugName);
}

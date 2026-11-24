package org.csu.lumtrace.lumtracediagnosis.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 药品饮食禁忌规则
 */
@Data
public class DrugFoodRule implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    /** 药品名称 */
    private String drugName;
    /** 禁忌食物 */
    private String forbidFood;
    /** 推荐饮食 */
    private String suggestFood;
    /** 提示说明 */
    private String tip;
    private LocalDateTime createTime;
}

package org.csu.lumtrace.lumtracediagnosis.mapper;

import org.apache.ibatis.annotations.*;
import org.csu.lumtrace.lumtracediagnosis.entity.DrugFoodRule;

import java.util.List;

@Mapper
public interface DrugFoodRuleMapper {

    @Select("SELECT * FROM drug_food_rule")
    List<DrugFoodRule> selectAll();

    @Select("SELECT * FROM drug_food_rule WHERE drug_name LIKE CONCAT('%', #{drugName}, '%')")
    List<DrugFoodRule> selectByDrugName(@Param("drugName") String drugName);

    @Select("SELECT * FROM drug_food_rule WHERE id = #{id}")
    DrugFoodRule selectById(@Param("id") Long id);

    @Insert("INSERT INTO drug_food_rule(drug_name, forbid_food, suggest_food, tip) VALUES(#{drugName}, #{forbidFood}, #{suggestFood}, #{tip})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(DrugFoodRule rule);
}

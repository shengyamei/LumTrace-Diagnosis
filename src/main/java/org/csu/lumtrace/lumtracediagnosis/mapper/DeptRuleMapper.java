package org.csu.lumtrace.lumtracediagnosis.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.csu.lumtrace.lumtracediagnosis.entity.DeptRule;

import java.util.List;

@Mapper
public interface DeptRuleMapper {

    @Select("SELECT * FROM t_dept_rule")
    List<DeptRule> selectAll();
}

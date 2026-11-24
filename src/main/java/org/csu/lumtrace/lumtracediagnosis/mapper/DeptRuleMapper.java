package org.csu.lumtrace.lumtracediagnosis.mapper;

import org.apache.ibatis.annotations.*;
import org.csu.lumtrace.lumtracediagnosis.entity.DeptRule;

import java.util.List;

@Mapper
public interface DeptRuleMapper {

    @Select("SELECT * FROM dept_rule")
    List<DeptRule> selectAll();

    @Select("SELECT * FROM dept_rule WHERE keyword LIKE CONCAT('%', #{keyword}, '%')")
    List<DeptRule> selectByKeyword(@Param("keyword") String keyword);

    @Select("SELECT * FROM dept_rule WHERE id = #{id}")
    DeptRule selectById(@Param("id") Long id);

    @Insert("INSERT INTO dept_rule(keyword, dept_name, tip) VALUES(#{keyword}, #{deptName}, #{tip})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(DeptRule rule);
}

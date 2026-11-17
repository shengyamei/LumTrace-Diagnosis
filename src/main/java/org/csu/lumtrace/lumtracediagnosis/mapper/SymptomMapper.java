package org.csu.lumtrace.lumtracediagnosis.mapper;

import org.apache.ibatis.annotations.*;
import org.csu.lumtrace.lumtracediagnosis.entity.Symptom;

import java.util.List;

@Mapper
public interface SymptomMapper {

    @Select("SELECT * FROM t_symptom WHERE id = #{id}")
    Symptom selectById(@Param("id") Long id);

    @Select("SELECT * FROM t_symptom WHERE status = 1 ORDER BY category, id")
    List<Symptom> selectAll();

    @Select("SELECT * FROM t_symptom WHERE category = #{category} AND status = 1")
    List<Symptom> selectByCategory(@Param("category") String category);

    @Select("<script>SELECT * FROM t_symptom WHERE id IN <foreach item='i' collection='ids' open='(' separator=',' close=')'>#{i}</foreach></script>")
    List<Symptom> selectByIds(@Param("ids") List<Long> ids);

    @Select("SELECT * FROM t_symptom WHERE status = 1 AND (name LIKE CONCAT('%',#{keyword},'%') OR description LIKE CONCAT('%',#{keyword},'%'))")
    List<Symptom> search(@Param("keyword") String keyword);

    @Insert("""
        INSERT INTO t_symptom(name, category, description, default_severity, status)
        VALUES(#{name}, #{category}, #{description}, #{defaultSeverity}, #{status})
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Symptom symptom);
}

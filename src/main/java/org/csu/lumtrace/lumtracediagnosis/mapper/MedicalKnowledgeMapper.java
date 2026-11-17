package org.csu.lumtrace.lumtracediagnosis.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.csu.lumtrace.lumtracediagnosis.entity.MedicalKnowledge;

import java.util.List;

@Mapper
public interface MedicalKnowledgeMapper {

    @Select("SELECT * FROM t_medical_knowledge")
    List<MedicalKnowledge> selectAll();

    @Select("SELECT * FROM t_medical_knowledge WHERE disease_name = #{diseaseName}")
    List<MedicalKnowledge> selectByDiseaseName(@Param("diseaseName") String diseaseName);
}

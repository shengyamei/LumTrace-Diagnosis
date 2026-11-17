package org.csu.lumtrace.lumtracediagnosis.mapper;

import org.apache.ibatis.annotations.*;
import org.csu.lumtrace.lumtracediagnosis.entity.DiagnosisRecord;

import java.util.List;

@Mapper
public interface DiagnosisRecordMapper {

    @Select("SELECT * FROM t_diagnosis_record WHERE id = #{id}")
    DiagnosisRecord selectById(@Param("id") Long id);

    @Select("SELECT * FROM t_diagnosis_record WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<DiagnosisRecord> selectByUserId(@Param("userId") Long userId);

    @Insert("""
        INSERT INTO t_diagnosis_record(user_id, symptom_ids, self_description, age, gender,
            ai_disease, confidence, ai_suggestion, candidate_diseases, status)
        VALUES(#{userId}, #{symptomIds}, #{selfDescription}, #{age}, #{gender},
            #{aiDisease}, #{confidence}, #{aiSuggestion}, #{candidateDiseases}, #{status})
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(DiagnosisRecord record);

    @Update("UPDATE t_diagnosis_record SET ai_disease=#{aiDisease}, confidence=#{confidence}, ai_suggestion=#{aiSuggestion}, candidate_diseases=#{candidateDiseases}, status=#{status} WHERE id=#{id}")
    int updateById(DiagnosisRecord record);
}

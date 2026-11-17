package org.csu.lumtrace.lumtracediagnosis.mapper;

import org.apache.ibatis.annotations.*;
import org.csu.lumtrace.lumtracediagnosis.entity.DiagnosisReport;

import java.util.List;

@Mapper
public interface DiagnosisReportMapper {

    @Select("SELECT * FROM t_diagnosis_report WHERE id = #{id}")
    DiagnosisReport selectById(@Param("id") Long id);

    @Select("SELECT * FROM t_diagnosis_report WHERE report_no = #{reportNo} LIMIT 1")
    DiagnosisReport selectByReportNo(@Param("reportNo") String reportNo);

    @Select("SELECT * FROM t_diagnosis_report WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<DiagnosisReport> selectByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM t_diagnosis_report WHERE doctor_id = #{doctorId} ORDER BY create_time DESC")
    List<DiagnosisReport> selectByDoctorId(@Param("doctorId") Long doctorId);

    @Insert("""
        INSERT INTO t_diagnosis_report(record_id, user_id, report_no, content, doctor_id, doctor_review, status)
        VALUES(#{recordId}, #{userId}, #{reportNo}, #{content}, #{doctorId}, #{doctorReview}, #{status})
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(DiagnosisReport report);

    @Update("UPDATE t_diagnosis_report SET doctor_id=#{doctorId}, doctor_review=#{doctorReview}, status=#{status} WHERE id=#{id}")
    int updateById(DiagnosisReport report);
}

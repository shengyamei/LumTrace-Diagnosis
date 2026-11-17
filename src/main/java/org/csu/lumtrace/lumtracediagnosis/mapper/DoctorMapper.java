package org.csu.lumtrace.lumtracediagnosis.mapper;

import org.apache.ibatis.annotations.*;
import org.csu.lumtrace.lumtracediagnosis.entity.Doctor;

import java.util.List;

@Mapper
public interface DoctorMapper {

    @Select("SELECT * FROM t_doctor WHERE id = #{id}")
    Doctor selectById(@Param("id") Long id);

    @Select("SELECT * FROM t_doctor WHERE user_id = #{userId} LIMIT 1")
    Doctor selectByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM t_doctor WHERE status = 1")
    List<Doctor> selectAll();

    @Select("SELECT * FROM t_doctor WHERE department = #{department} AND status = 1")
    List<Doctor> selectByDepartment(@Param("department") String department);

    @Insert("""
        INSERT INTO t_doctor(user_id, name, department, title, hospital, specialty, introduction, status)
        VALUES(#{userId}, #{name}, #{department}, #{title}, #{hospital}, #{specialty}, #{introduction}, #{status})
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Doctor doctor);

    @Update("UPDATE t_doctor SET name=#{name}, department=#{department}, title=#{title}, hospital=#{hospital}, specialty=#{specialty}, introduction=#{introduction} WHERE id=#{id}")
    int updateById(Doctor doctor);
}

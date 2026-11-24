package org.csu.lumtrace.lumtracediagnosis.mapper;

import org.apache.ibatis.annotations.*;
import org.csu.lumtrace.lumtracediagnosis.entity.DoctorOrder;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface DoctorOrderMapper {

    @Select("SELECT * FROM doctor_order WHERE id = #{id}")
    DoctorOrder selectById(@Param("id") Long id);

    @Select("SELECT * FROM doctor_order WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<DoctorOrder> selectByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM doctor_order WHERE is_finish = 0 AND start_date <= #{date} AND end_date >= #{date}")
    List<DoctorOrder> selectActiveOrders(@Param("date") LocalDate date);

    @Insert("""
        INSERT INTO doctor_order(user_id, drug_name, dosage, frequency, remind_time, start_date, end_date, is_finish)
        VALUES(#{userId}, #{drugName}, #{dosage}, #{frequency}, #{remindTime}, #{startDate}, #{endDate}, #{isFinish})
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(DoctorOrder order);

    @Update("""
        <script>
        UPDATE doctor_order
        <set>
          <if test="drugName != null">drug_name=#{drugName},</if>
          <if test="dosage != null">dosage=#{dosage},</if>
          <if test="frequency != null">frequency=#{frequency},</if>
          <if test="remindTime != null">remind_time=#{remindTime},</if>
          <if test="startDate != null">start_date=#{startDate},</if>
          <if test="endDate != null">end_date=#{endDate},</if>
          <if test="isFinish != null">is_finish=#{isFinish},</if>
        </set>
        WHERE id=#{id}
        </script>
    """)
    int updateById(DoctorOrder order);

    @Delete("DELETE FROM doctor_order WHERE id = #{id} AND user_id = #{userId}")
    int deleteById(@Param("id") Long id, @Param("userId") Long userId);
}

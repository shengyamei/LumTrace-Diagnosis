package org.csu.lumtrace.lumtracediagnosis.mapper;

import org.apache.ibatis.annotations.*;
import org.csu.lumtrace.lumtracediagnosis.entity.DiseaseRecord;

import java.util.List;

@Mapper
public interface DiseaseRecordMapper {

    @Select("SELECT * FROM disease_record WHERE id = #{id}")
    DiseaseRecord selectById(@Param("id") Long id);

    @Select("SELECT * FROM disease_record WHERE user_id = #{userId} ORDER BY record_time DESC")
    List<DiseaseRecord> selectByUserId(@Param("userId") Long userId);

    @Insert("""
        INSERT INTO disease_record(user_id, record_time, symptom, temperature, pain_score, remark, img_url)
        VALUES(#{userId}, #{recordTime}, #{symptom}, #{temperature}, #{painScore}, #{remark}, #{imgUrl})
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(DiseaseRecord record);

    @Update("""
        <script>
        UPDATE disease_record
        <set>
          <if test="recordTime != null">record_time=#{recordTime},</if>
          <if test="symptom != null">symptom=#{symptom},</if>
          <if test="temperature != null">temperature=#{temperature},</if>
          <if test="painScore != null">pain_score=#{painScore},</if>
          <if test="remark != null">remark=#{remark},</if>
          <if test="imgUrl != null">img_url=#{imgUrl},</if>
        </set>
        WHERE id=#{id} AND user_id=#{userId}
        </script>
    """)
    int updateById(DiseaseRecord record);

    @Delete("DELETE FROM disease_record WHERE id = #{id} AND user_id = #{userId}")
    int deleteById(@Param("id") Long id, @Param("userId") Long userId);
}

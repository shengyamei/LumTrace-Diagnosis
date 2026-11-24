package org.csu.lumtrace.lumtracediagnosis.mapper;

import org.apache.ibatis.annotations.*;
import org.csu.lumtrace.lumtracediagnosis.entity.User;

@Mapper
public interface UserMapper {

    @Select("SELECT * FROM `user` WHERE username = #{username} LIMIT 1")
    User selectByUsername(@Param("username") String username);

    @Select("SELECT * FROM `user` WHERE id = #{id}")
    User selectById(@Param("id") Long id);

    @Insert("""
        INSERT INTO `user`(username, password, nickname, phone, status)
        VALUES(#{username}, #{password}, #{nickname}, #{phone}, #{status})
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    @Update("""
        <script>
        UPDATE `user`
        <set>
          <if test="password != null">password=#{password},</if>
          <if test="nickname != null">nickname=#{nickname},</if>
          <if test="phone != null">phone=#{phone},</if>
          <if test="status != null">status=#{status},</if>
        </set>
        WHERE id=#{id}
        </script>
    """)
    int updateById(User user);

    @Select("SELECT COUNT(1) FROM `user` WHERE username = #{username}")
    int countByUsername(@Param("username") String username);
}

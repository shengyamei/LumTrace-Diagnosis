package org.csu.lumtrace.lumtracediagnosis.mapper;

import org.apache.ibatis.annotations.*;
import org.csu.lumtrace.lumtracediagnosis.entity.RemindLog;

import java.util.List;

@Mapper
public interface RemindLogMapper {

    @Select("SELECT * FROM remind_log WHERE id = #{id}")
    RemindLog selectById(@Param("id") Long id);

    @Select("SELECT * FROM remind_log WHERE user_id = #{userId} ORDER BY remind_time DESC")
    List<RemindLog> selectByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM remind_log WHERE order_id = #{orderId} ORDER BY remind_time DESC")
    List<RemindLog> selectByOrderId(@Param("orderId") Long orderId);

    @Insert("""
        INSERT INTO remind_log(order_id, user_id, remind_time, is_read, is_taken)
        VALUES(#{orderId}, #{userId}, #{remindTime}, #{isRead}, #{isTaken})
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(RemindLog log);

    @Update("UPDATE remind_log SET is_read = #{isRead}, is_taken = #{isTaken} WHERE id = #{id}")
    int updateStatus(RemindLog remindLog);

    @Select("SELECT COUNT(1) FROM remind_log WHERE order_id = #{orderId} AND remind_time >= #{start} AND remind_time < #{end}")
    int countByOrderAndDate(@Param("orderId") Long orderId, @Param("start") java.time.LocalDateTime start, @Param("end") java.time.LocalDateTime end);

    /** 查询用户未读提醒（客户端轮询用） */
    @Select("SELECT * FROM remind_log WHERE user_id = #{userId} AND is_read = 0 ORDER BY remind_time DESC")
    List<RemindLog> selectUnreadByUserId(@Param("userId") Long userId);

    /** 批量标记为已读 */
    @Update("UPDATE remind_log SET is_read = 1 WHERE user_id = #{userId} AND is_read = 0")
    int markAllRead(@Param("userId") Long userId);
}

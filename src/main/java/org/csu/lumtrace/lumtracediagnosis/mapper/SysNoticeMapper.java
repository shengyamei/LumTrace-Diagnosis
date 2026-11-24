package org.csu.lumtrace.lumtracediagnosis.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.csu.lumtrace.lumtracediagnosis.entity.SysNotice;

import java.util.List;

@Mapper
public interface SysNoticeMapper {

    @Select("SELECT * FROM sys_notice WHERE type = #{type}")
    List<SysNotice> selectByType(String type);

    @Select("SELECT * FROM sys_notice ORDER BY create_time DESC")
    List<SysNotice> selectAll();
}

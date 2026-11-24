package org.csu.lumtrace.lumtracediagnosis.controller;

import org.csu.lumtrace.lumtracediagnosis.dto.response.Result;
import org.csu.lumtrace.lumtracediagnosis.entity.SysNotice;
import org.csu.lumtrace.lumtracediagnosis.mapper.SysNoticeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 公告与免责声明接口（无需登录）
 */
@RestController
@RequestMapping("/notice")
public class NoticeController {

    @Autowired
    private SysNoticeMapper sysNoticeMapper;

    /**
     * 获取免责声明列表
     * GET /notice/disclaimers
     */
    @GetMapping("/disclaimers")
    public Result<List<SysNotice>> disclaimers() {
        return Result.success(sysNoticeMapper.selectByType("DISCLAIMER"));
    }

    /**
     * 获取系统公告列表
     * GET /notice/list?type=NOTICE
     */
    @GetMapping("/list")
    public Result<List<SysNotice>> list(@RequestParam(required = false) String type) {
        if (type != null && !type.isBlank()) {
            return Result.success(sysNoticeMapper.selectByType(type));
        }
        return Result.success(sysNoticeMapper.selectAll());
    }
}

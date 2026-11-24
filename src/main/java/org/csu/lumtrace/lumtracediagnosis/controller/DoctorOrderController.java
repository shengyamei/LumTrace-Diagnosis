package org.csu.lumtrace.lumtracediagnosis.controller;

import org.csu.lumtrace.lumtracediagnosis.common.util.UserContext;
import org.csu.lumtrace.lumtracediagnosis.dto.response.Result;
import org.csu.lumtrace.lumtracediagnosis.entity.DoctorOrder;
import org.csu.lumtrace.lumtracediagnosis.entity.RemindLog;
import org.csu.lumtrace.lumtracediagnosis.service.DoctorOrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 医嘱与定时提醒接口（队长/成员A 负责）
 * 包含：医嘱增删改查 + 未读提醒轮询 + 标记已服药 + 结束疗程
 */
@RestController
@RequestMapping("/order")
public class DoctorOrderController {

    @Autowired
    private DoctorOrderService orderService;

    // ==================== 医嘱 CRUD ====================

    /**
     * 新增医嘱
     * POST /order
     */
    @PostMapping
    public Result<DoctorOrder> create(@Valid @RequestBody DoctorOrder order) {
        Long userId = UserContext.getUserId();
        return Result.success(orderService.create(userId, order));
    }

    /**
     * 修改医嘱
     * PUT /order/{id}
     */
    @PutMapping("/{id}")
    public Result<DoctorOrder> update(@PathVariable Long id, @RequestBody DoctorOrder order) {
        Long userId = UserContext.getUserId();
        order.setId(id);
        return Result.success(orderService.update(userId, order));
    }

    /**
     * 删除医嘱
     * DELETE /order/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        orderService.delete(userId, id);
        return Result.success();
    }

    /**
     * 查询当前用户所有医嘱（倒序）
     * GET /order/list
     */
    @GetMapping("/list")
    public Result<List<DoctorOrder>> list() {
        Long userId = UserContext.getUserId();
        return Result.success(orderService.listByUser(userId));
    }

    /**
     * 结束疗程（手动标记医嘱已完成）
     * PUT /order/{id}/finish
     */
    @PutMapping("/{id}/finish")
    public Result<Void> finishOrder(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        orderService.finishOrder(userId, id);
        return Result.success();
    }

    // ==================== 提醒记录 ====================

    /**
     * 查询当前用户提醒历史记录（全部，倒序）
     * GET /order/remind-history
     */
    @GetMapping("/remind-history")
    public Result<List<RemindLog>> remindHistory() {
        Long userId = UserContext.getUserId();
        return Result.success(orderService.remindHistory(userId));
    }

    /**
     * 轮询接口：获取未读提醒列表，客户端收到后触发 Android 本地通知
     * GET /order/pending-reminds
     */
    @GetMapping("/pending-reminds")
    public Result<List<RemindLog>> pendingReminds() {
        Long userId = UserContext.getUserId();
        return Result.success(orderService.getPendingReminds(userId));
    }

    /**
     * 将所有未读提醒批量标记为已读（客户端展示通知后调用）
     * PUT /order/reminds/read-all
     */
    @PutMapping("/reminds/read-all")
    public Result<Void> markAllRead() {
        Long userId = UserContext.getUserId();
        orderService.markAllRemindsRead(userId);
        return Result.success();
    }

    /**
     * 标记某条提醒为"已服药"（对应客户端【已服药】按钮）
     * PUT /order/reminds/{remindLogId}/taken
     */
    @PutMapping("/reminds/{remindLogId}/taken")
    public Result<Void> markTaken(@PathVariable Long remindLogId) {
        orderService.markTaken(remindLogId);
        return Result.success();
    }
}

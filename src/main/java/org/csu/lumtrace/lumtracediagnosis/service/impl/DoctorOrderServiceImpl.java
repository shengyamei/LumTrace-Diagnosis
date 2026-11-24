package org.csu.lumtrace.lumtracediagnosis.service.impl;

import org.csu.lumtrace.lumtracediagnosis.common.exception.BusinessException;
import org.csu.lumtrace.lumtracediagnosis.entity.DoctorOrder;
import org.csu.lumtrace.lumtracediagnosis.entity.RemindLog;
import org.csu.lumtrace.lumtracediagnosis.mapper.DoctorOrderMapper;
import org.csu.lumtrace.lumtracediagnosis.mapper.RemindLogMapper;
import org.csu.lumtrace.lumtracediagnosis.service.DoctorOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 医嘱与定时提醒服务实现
 */
@Service
public class DoctorOrderServiceImpl implements DoctorOrderService {

    @Autowired
    private DoctorOrderMapper orderMapper;

    @Autowired
    private RemindLogMapper remindLogMapper;

    @Override
    public DoctorOrder create(Long userId, DoctorOrder order) {
        order.setUserId(userId);
        order.setIsFinish(0);
        if (order.getStartDate() == null) order.setStartDate(LocalDate.now());
        if (order.getEndDate() == null) {
            throw new BusinessException(400, "请设置结束日期");
        }
        orderMapper.insert(order);
        return order;
    }

    @Override
    public DoctorOrder update(Long userId, DoctorOrder order) {
        DoctorOrder existing = orderMapper.selectById(order.getId());
        if (existing == null || !existing.getUserId().equals(userId)) {
            throw new BusinessException(404, "医嘱不存在或无权限修改");
        }
        orderMapper.updateById(order);
        return orderMapper.selectById(order.getId());
    }

    @Override
    public void delete(Long userId, Long id) {
        int rows = orderMapper.deleteById(id, userId);
        if (rows == 0) throw new BusinessException(404, "医嘱不存在或无权限删除");
    }

    @Override
    public List<DoctorOrder> listByUser(Long userId) {
        return orderMapper.selectByUserId(userId);
    }

    @Override
    public List<RemindLog> remindHistory(Long userId) {
        return remindLogMapper.selectByUserId(userId);
    }

    @Override
    public List<RemindLog> getPendingReminds(Long userId) {
        return remindLogMapper.selectUnreadByUserId(userId);
    }

    @Override
    public void markAllRemindsRead(Long userId) {
        remindLogMapper.markAllRead(userId);
    }

    @Override
    public void markTaken(Long remindLogId) {
        RemindLog log = remindLogMapper.selectById(remindLogId);
        if (log == null) throw new BusinessException(404, "提醒记录不存在");
        log.setIsRead(1);
        log.setIsTaken(1);
        remindLogMapper.updateStatus(log);
    }

    @Override
    public void finishOrder(Long userId, Long orderId) {
        DoctorOrder order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(404, "医嘱不存在或无权限操作");
        }
        DoctorOrder update = new DoctorOrder();
        update.setId(orderId);
        update.setIsFinish(1);
        orderMapper.updateById(update);
    }
}

package org.csu.lumtrace.lumtracediagnosis.service;

import org.csu.lumtrace.lumtracediagnosis.entity.DoctorOrder;
import org.csu.lumtrace.lumtracediagnosis.entity.RemindLog;

import java.util.List;

/**
 * 医嘱与定时提醒服务
 */
public interface DoctorOrderService {
    /** 新增医嘱 */
    DoctorOrder create(Long userId, DoctorOrder order);

    /** 修改医嘱 */
    DoctorOrder update(Long userId, DoctorOrder order);

    /** 删除医嘱 */
    void delete(Long userId, Long id);

    /** 查询用户所有医嘱 */
    List<DoctorOrder> listByUser(Long userId);

    /** 查询提醒记录 */
    List<RemindLog> remindHistory(Long userId);

    /** 获取当前用户未读提醒列表（客户端轮询触发本地通知） */
    List<RemindLog> getPendingReminds(Long userId);

    /** 将当前用户所有未读提醒标记为已读 */
    void markAllRemindsRead(Long userId);

    /** 标记已服药 */
    void markTaken(Long remindLogId);

    /** 结束疗程 */
    void finishOrder(Long userId, Long orderId);
}

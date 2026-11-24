package org.csu.lumtrace.lumtracediagnosis.task;

import org.csu.lumtrace.lumtracediagnosis.entity.DoctorOrder;
import org.csu.lumtrace.lumtracediagnosis.entity.RemindLog;
import org.csu.lumtrace.lumtracediagnosis.mapper.DoctorOrderMapper;
import org.csu.lumtrace.lumtracediagnosis.mapper.RemindLogMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * 医嘱定时提醒任务：每分钟扫描活跃医嘱，在到达提醒时间时生成提醒记录
 */
@Component
public class RemindTask {
    private static final Logger log = LoggerFactory.getLogger(RemindTask.class);

    @Autowired
    private DoctorOrderMapper orderMapper;

    @Autowired
    private RemindLogMapper remindLogMapper;

    /**
     * 每分钟执行一次，检查当前时间是否匹配活跃医嘱的提醒时间
     */
    @Scheduled(fixedRateString = "${lumtrace.remind.scan-interval-ms:60000}")
    public void scanAndRemind() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        int currentHour = now.getHour();
        int currentMinute = now.getMinute();

        // 查询今天有效的活跃医嘱
        List<DoctorOrder> activeOrders = orderMapper.selectActiveOrders(today);

        for (DoctorOrder order : activeOrders) {
            if (order.getRemindTime() == null || order.getRemindTime().isBlank()) continue;

            // 解析提醒时间（格式：HH:mm,HH:mm）
            String[] times = order.getRemindTime().split(",");
            for (String timeStr : times) {
                timeStr = timeStr.trim();
                if (timeStr.isEmpty()) continue;

                try {
                    String[] parts = timeStr.split(":");
                    int h = Integer.parseInt(parts[0]);
                    int m = Integer.parseInt(parts[1]);

                    // 当前时间匹配提醒时间（精确到分钟）
                    if (h == currentHour && m == currentMinute) {
                        // 检查是否已经生成过本条提醒（防重复）
                        LocalDateTime remindDateTime = LocalDateTime.of(today, LocalTime.of(h, m));
                        int existing = remindLogMapper.countByOrderAndDate(
                                order.getId(),
                                remindDateTime,
                                remindDateTime.plusMinutes(1)
                        );

                        if (existing == 0) {
                            // 生成提醒记录
                            RemindLog remindLog = new RemindLog();
                            remindLog.setOrderId(order.getId());
                            remindLog.setUserId(order.getUserId());
                            remindLog.setRemindTime(remindDateTime);
                            remindLog.setIsRead(0);
                            remindLog.setIsTaken(0);
                            remindLogMapper.insert(remindLog);

                            log.info("生成服药提醒: userId={}, drug={}, time={}",
                                    order.getUserId(), order.getDrugName(), timeStr);
                        }
                    }
                } catch (NumberFormatException e) {
                    log.warn("提醒时间格式错误: order={}, remindTime={}", order.getId(), timeStr);
                }
            }

            // 检查是否已过结束日期，自动标记完成
            if (today.isAfter(order.getEndDate())) {
                DoctorOrder update = new DoctorOrder();
                update.setId(order.getId());
                update.setIsFinish(1);
                orderMapper.updateById(update);
                log.info("医嘱疗程已自动结束: orderId={}, drug={}", order.getId(), order.getDrugName());
            }
        }
    }
}

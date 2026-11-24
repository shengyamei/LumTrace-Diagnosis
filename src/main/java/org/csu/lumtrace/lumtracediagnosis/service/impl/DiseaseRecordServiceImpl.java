package org.csu.lumtrace.lumtracediagnosis.service.impl;

import org.csu.lumtrace.lumtracediagnosis.common.exception.BusinessException;
import org.csu.lumtrace.lumtracediagnosis.entity.DiseaseRecord;
import org.csu.lumtrace.lumtracediagnosis.mapper.DiseaseRecordMapper;
import org.csu.lumtrace.lumtracediagnosis.service.DiseaseRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 病程记录服务实现：增删改查 + 时间线排序 + 文本导出
 */
@Service
public class DiseaseRecordServiceImpl implements DiseaseRecordService {

    @Autowired
    private DiseaseRecordMapper recordMapper;

    @Override
    public DiseaseRecord create(Long userId, DiseaseRecord record) {
        record.setUserId(userId);
        if (record.getRecordTime() == null) {
            record.setRecordTime(java.time.LocalDateTime.now());
        }
        recordMapper.insert(record);
        return record;
    }

    @Override
    public DiseaseRecord update(Long userId, DiseaseRecord record) {
        record.setUserId(userId);
        int rows = recordMapper.updateById(record);
        if (rows == 0) throw new BusinessException(404, "记录不存在或无权限修改");
        return recordMapper.selectById(record.getId());
    }

    @Override
    public void delete(Long userId, Long id) {
        int rows = recordMapper.deleteById(id, userId);
        if (rows == 0) throw new BusinessException(404, "记录不存在或无权限删除");
    }

    @Override
    public List<DiseaseRecord> timeline(Long userId) {
        return recordMapper.selectByUserId(userId);
    }

    @Override
    public String exportText(Long userId) {
        List<DiseaseRecord> records = recordMapper.selectByUserId(userId);
        if (records.isEmpty()) {
            return "暂无病程记录。";
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        StringBuilder sb = new StringBuilder();
        sb.append("========== 病程记录 ==========\n");
        sb.append("导出时间：").append(java.time.LocalDateTime.now().format(fmt)).append("\n");
        sb.append("记录条数：").append(records.size()).append("\n\n");

        // 按时间正序展示（方便医生阅读）；records 已经是倒序，反转得到正序
        List<DiseaseRecord> sorted = new java.util.ArrayList<>(records);
        java.util.Collections.reverse(sorted);
        for (int i = 0; i < sorted.size(); i++) {
            DiseaseRecord r = sorted.get(i);
            sb.append("【").append(i + 1).append("】")
                    .append(r.getRecordTime() != null ? r.getRecordTime().format(fmt) : "未设置时间")
                    .append("\n");
            if (r.getSymptom() != null && !r.getSymptom().isBlank()) {
                sb.append("  症状：").append(r.getSymptom()).append("\n");
            }
            if (r.getTemperature() != null) {
                sb.append("  体温：").append(r.getTemperature()).append("℃\n");
            }
            if (r.getPainScore() != null) {
                sb.append("  疼痛评分：").append(r.getPainScore()).append("/10\n");
            }
            if (r.getRemark() != null && !r.getRemark().isBlank()) {
                sb.append("  备注：").append(r.getRemark()).append("\n");
            }
            sb.append("\n");
        }
        sb.append("========== 以上记录仅供参考，请以医生面诊为准 ==========\n");
        return sb.toString();
    }
}

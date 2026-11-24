package org.csu.lumtrace.lumtracediagnosis.service;

import org.csu.lumtrace.lumtracediagnosis.entity.DiseaseRecord;

import java.util.List;

/**
 * 病程记录服务
 */
public interface DiseaseRecordService {
    /** 新增记录 */
    DiseaseRecord create(Long userId, DiseaseRecord record);

    /** 修改记录 */
    DiseaseRecord update(Long userId, DiseaseRecord record);

    /** 删除记录 */
    void delete(Long userId, Long id);

    /** 按时间倒序查询时间线 */
    List<DiseaseRecord> timeline(Long userId);

    /** 导出病程文本（可直接复制给医生） */
    String exportText(Long userId);
}

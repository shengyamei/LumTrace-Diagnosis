package org.csu.lumtrace.lumtracediagnosis.service;

import org.csu.lumtrace.lumtracediagnosis.dto.request.DiagnosisRequest;
import org.csu.lumtrace.lumtracediagnosis.dto.response.DiagnosisResponse;
import org.csu.lumtrace.lumtracediagnosis.entity.DiagnosisRecord;

import java.util.List;

public interface DiagnosisService {
    /** 执行一次AI诊断并落库 */
    DiagnosisResponse diagnose(Long userId, DiagnosisRequest req);

    /** 查询某用户历史诊断记录 */
    List<DiagnosisRecord> history(Long userId);

    DiagnosisRecord getById(Long id);
}

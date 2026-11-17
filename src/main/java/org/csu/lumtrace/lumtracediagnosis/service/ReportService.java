package org.csu.lumtrace.lumtracediagnosis.service;

import org.csu.lumtrace.lumtracediagnosis.entity.DiagnosisReport;

import java.util.List;

public interface ReportService {
    List<DiagnosisReport> listByUser(Long userId);
    DiagnosisReport getByNo(String reportNo);
    /** 医生复核 */
    DiagnosisReport review(Long doctorId, Long reportId, String review);
}

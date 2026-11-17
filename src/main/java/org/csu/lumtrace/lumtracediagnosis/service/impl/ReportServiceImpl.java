package org.csu.lumtrace.lumtracediagnosis.service.impl;

import org.csu.lumtrace.lumtracediagnosis.common.constant.AppConstants;
import org.csu.lumtrace.lumtracediagnosis.common.exception.BusinessException;
import org.csu.lumtrace.lumtracediagnosis.entity.DiagnosisReport;
import org.csu.lumtrace.lumtracediagnosis.mapper.DiagnosisReportMapper;
import org.csu.lumtrace.lumtracediagnosis.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private DiagnosisReportMapper reportMapper;

    @Override
    public List<DiagnosisReport> listByUser(Long userId) {
        return reportMapper.selectByUserId(userId);
    }

    @Override
    public DiagnosisReport getByNo(String reportNo) {
        DiagnosisReport r = reportMapper.selectByReportNo(reportNo);
        if (r == null) throw new BusinessException(404, "报告不存在");
        return r;
    }

    @Override
    public DiagnosisReport review(Long doctorId, Long reportId, String review) {
        DiagnosisReport r = reportMapper.selectById(reportId);
        if (r == null) throw new BusinessException(404, "报告不存在");
        r.setDoctorId(doctorId);
        r.setDoctorReview(review);
        r.setStatus(AppConstants.REPORT_DOCTOR_REVIEWED);
        reportMapper.updateById(r);
        return r;
    }
}

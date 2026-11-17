package org.csu.lumtrace.lumtracediagnosis.service.impl;

import org.csu.lumtrace.lumtracediagnosis.ai.DiagnosisEngineRouter;
import org.csu.lumtrace.lumtracediagnosis.common.constant.AppConstants;
import org.csu.lumtrace.lumtracediagnosis.common.exception.BusinessException;
import org.csu.lumtrace.lumtracediagnosis.dto.request.DiagnosisRequest;
import org.csu.lumtrace.lumtracediagnosis.dto.response.DiagnosisResponse;
import org.csu.lumtrace.lumtracediagnosis.entity.DiagnosisRecord;
import org.csu.lumtrace.lumtracediagnosis.entity.DiagnosisReport;
import org.csu.lumtrace.lumtracediagnosis.mapper.DiagnosisRecordMapper;
import org.csu.lumtrace.lumtracediagnosis.mapper.DiagnosisReportMapper;
import org.csu.lumtrace.lumtracediagnosis.service.DiagnosisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DiagnosisServiceImpl implements DiagnosisService {
    private static final Logger log = LoggerFactory.getLogger(DiagnosisServiceImpl.class);

    @Autowired
    private DiagnosisEngineRouter engineRouter;

    @Autowired
    private DiagnosisRecordMapper recordMapper;

    @Autowired
    private DiagnosisReportMapper reportMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DiagnosisResponse diagnose(Long userId, DiagnosisRequest req) {
        if (req.getSymptomIds() == null || req.getSymptomIds().isEmpty()) {
            throw new BusinessException(400, "请至少选择一个症状");
        }
        Map<Long, String> nameMap = engineRouter.resolveSymptomNames(req.getSymptomIds());
        if (nameMap.isEmpty()) throw new BusinessException(400, "症状ID不存在");

        DiagnosisResponse resp = engineRouter.diagnose(req, nameMap);

        // 落库
        DiagnosisRecord record = new DiagnosisRecord();
        record.setUserId(userId);
        record.setSymptomIds(req.getSymptomIds().stream().map(String::valueOf).collect(Collectors.joining(",")));
        record.setSelfDescription(req.getSelfDescription());
        record.setAge(req.getAge());
        record.setGender(req.getGender());
        record.setAiDisease(resp.getTopDisease());
        record.setConfidence(resp.getConfidence());
        record.setAiSuggestion(resp.getSuggestion());
        record.setCandidateDiseases(serializeCandidates(resp.getCandidates()));
        record.setStatus(AppConstants.RECORD_PENDING);
        recordMapper.insert(record);
        resp.setRecordId(record.getId());

        // 自动生成诊断报告
        DiagnosisReport report = new DiagnosisReport();
        report.setRecordId(record.getId());
        report.setUserId(userId);
        report.setReportNo("RPT" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")));
        report.setContent(buildReportContent(record, resp, nameMap));
        report.setStatus(AppConstants.REPORT_AI_ONLY);
        reportMapper.insert(report);

        return resp;
    }

    @Override
    public List<DiagnosisRecord> history(Long userId) {
        return recordMapper.selectByUserId(userId);
    }

    @Override
    public DiagnosisRecord getById(Long id) {
        DiagnosisRecord r = recordMapper.selectById(id);
        if (r == null) throw new BusinessException(404, "诊断记录不存在");
        return r;
    }

    /** 手写 JSON 序列化，避免 Jackson 3 包名迁移影响 */
    private String serializeCandidates(List<DiagnosisResponse.Candidate> list) {
        if (list == null || list.isEmpty()) return "[]";
        String Q = "\"";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            DiagnosisResponse.Candidate c = list.get(i);
            sb.append("{")
                    .append(Q).append("disease").append(Q).append(":")
                    .append(Q).append(escape(c.getDisease())).append(Q).append(",")
                    .append(Q).append("score").append(Q).append(":").append(c.getScore()).append(",")
                    .append(Q).append("hitSymptoms").append(Q).append(":[");
            if (c.getHitSymptoms() != null) {
                for (int j = 0; j < c.getHitSymptoms().size(); j++) {
                    if (j > 0) sb.append(',');
                    sb.append(Q).append(escape(c.getHitSymptoms().get(j))).append(Q);
                }
            }
            sb.append("]}");
            if (i < list.size() - 1) sb.append(',');
        }
        sb.append(']');
        return sb.toString();
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String buildReportContent(DiagnosisRecord record, DiagnosisResponse resp, Map<Long, String> nameMap) {
        StringBuilder sb = new StringBuilder();
        sb.append("== LumTrace AI 诊断报告 ==\n");
        sb.append("报告编号：见系统\n");
        String symptomText = java.util.Arrays.stream(record.getSymptomIds().split(","))
                .filter(s -> !s.isBlank())
                .map(s -> nameMap.getOrDefault(Long.valueOf(s.trim()), s))
                .collect(Collectors.joining("、"));
        sb.append("症状：").append(symptomText.isEmpty() ? "无" : symptomText).append("\n");
        if (record.getSelfDescription() != null && !record.getSelfDescription().isBlank()) {
            sb.append("自述：").append(record.getSelfDescription()).append("\n");
        }
        sb.append("AI结论：").append(resp.getTopDisease()).append("\n");
        sb.append("置信度：").append(resp.getConfidence()).append("\n");
        sb.append("严重度：").append(resp.getSeverity()).append("\n");
        sb.append("建议：").append(resp.getSuggestion()).append("\n");
        sb.append("\n提示：本报告由AI生成，仅供参考，不作为医学诊断结论。\n");
        return sb.toString();
    }
}

package com.sahinh.io.vn.api.SH.A0.SHA0_0500;

import com.sahinh.io.vn.core.dataset.DataSetRegistry;
import com.sahinh.io.vn.core.exam.ExamService;
import java.util.Map;
import org.springframework.stereotype.Component;

/** mod — Thi Luồng 2 — Bấm chay chi tiết: thầy bấm + / - theo từng bước nhỏ.. Nghiệp vụ thi nằm trong ExamService dùng chung. */
@Component
public class SHA0_0500_mod {
    private final ExamService exam;

    public SHA0_0500_mod(DataSetRegistry registry) {
        this.exam = new ExamService(registry);
    }

    public Map<String, Object> plan(long presetId) {
        return exam.planFor(presetId, 2);
    }

    public Map<String, Object> start(long userId, long presetId, String studentName) {
        return exam.startSession(userId, presetId, 2, studentName);
    }

    public Map<String, Object> log(long sessionId, Integer lessonNo, Integer stepNo, Long errorRuleId) {
        return exam.logEvent(sessionId, lessonNo, stepNo, errorRuleId);
    }

    public Map<String, Object> finish(long sessionId, boolean failed, String failedStep, String failedReason) {
        return exam.finishSession(sessionId, failed, failedStep, failedReason);
    }

    public Map<String, Object> result(long sessionId) {
        return exam.resultOf(sessionId);
    }
}

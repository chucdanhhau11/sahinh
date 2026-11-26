package com.sahinh.io.vn.api.SH.A0.SHA0_0600;

import com.sahinh.io.vn.core.dataset.DataSetRegistry;
import com.sahinh.io.vn.core.exam.ExamService;
import java.util.Map;
import org.springframework.stereotype.Component;

/** mod — Thi Luồng 3 — Bấm chay thi thật: không bước nhỏ, chỉ 14 mục, thầy phải bấm mới qua bài.. Nghiệp vụ thi nằm trong ExamService dùng chung. */
@Component
public class SHA0_0600_mod {
    private final ExamService exam;

    public SHA0_0600_mod(DataSetRegistry registry) {
        this.exam = new ExamService(registry);
    }

    public Map<String, Object> plan(long presetId, String transmission) {
        return exam.planFor(presetId, 3, transmission);
    }

    public Map<String, Object> start(long userId, long presetId, String studentName, String transmission) {
        return exam.startSession(userId, presetId, 3, studentName, transmission);
    }

    public Map<String, Object> log(long sessionId, Integer lessonNo, Integer stepNo, Long errorRuleId, Long userId, String role) {
        return exam.logEvent(sessionId, lessonNo, stepNo, errorRuleId, userId, role);
    }

    public Map<String, Object> finish(long sessionId, boolean failed, String failedStep, String failedReason, Long userId, String role) {
        return exam.finishSession(sessionId, failed, failedStep, failedReason, userId, role);
    }

    public Map<String, Object> result(long sessionId, Long userId, String role) {
        return exam.resultOf(sessionId, userId, role);
    }
}

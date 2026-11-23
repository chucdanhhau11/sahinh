package com.sahinh.io.vn.api.SH.A0.SHA0_0400;

import com.sahinh.io.vn.core.dataset.DataSetRegistry;
import com.sahinh.io.vn.core.exam.ExamService;
import java.util.Map;
import org.springframework.stereotype.Component;

/** mod — Thi Luồng 1 — Tự động: thầy chỉ bấm Bắt đầu 1 lần, máy tự chạy hết; sai thì dừng, làm lại từ đầu.. Nghiệp vụ thi nằm trong ExamService dùng chung. */
@Component
public class SHA0_0400_mod {
    private final ExamService exam;

    public SHA0_0400_mod(DataSetRegistry registry) {
        this.exam = new ExamService(registry);
    }

    public Map<String, Object> plan(long presetId, String transmission) {
        return exam.planFor(presetId, 1, transmission);
    }

    public Map<String, Object> start(long userId, long presetId, String studentName, String transmission) {
        return exam.startSession(userId, presetId, 1, studentName, transmission);
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

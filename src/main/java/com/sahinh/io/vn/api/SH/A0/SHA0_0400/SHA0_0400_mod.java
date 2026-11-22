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

    public Map<String, Object> plan(long presetId) {
        return exam.planFor(presetId, 1);
    }

    public Map<String, Object> start(long userId, long presetId, String studentName) {
        return exam.startSession(userId, presetId, 1, studentName);
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

package com.sahinh.io.vn.core.exam;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ExamEngineTest {

    @Test
    void passThresholdIs80() {
        assertTrue(ExamEngine.isPass(80));
        assertTrue(ExamEngine.isPass(100));
        assertFalse(ExamEngine.isPass(79));
    }

    @Test
    void nonLietErrorDeductsAndContinues() {
        Map<String, Object> rule = Map.of("NAME", "Dừng cách vạch quá 500mm",
                "DEDUCT_POINTS", 5, "IS_LIET", 0);
        ExamEngine.ApplyResult r = ExamEngine.applyError(100, rule);
        assertEquals(95, r.score());
        assertFalse(r.isLiet());
        assertFalse(r.finished());
    }

    @Test
    void lietErrorStopsImmediately() {
        Map<String, Object> rule = Map.of("NAME", "Tắt máy trên dốc",
                "DEDUCT_POINTS", 0, "IS_LIET", 1);
        ExamEngine.ApplyResult r = ExamEngine.applyError(100, rule);
        assertTrue(r.isLiet());
        assertTrue(r.finished());
        assertTrue(r.failedReason().contains("Tắt máy trên dốc"));
    }

    @Test
    void flow1TimeoutFailsStrict() {
        ExamEngine.ApplyResult r = ExamEngine.failStrict("Bài 1 - bước 2");
        assertEquals(0, r.score());
        assertTrue(r.finished());
    }

    @Test
    void buildPlanExpandsStepsWhenDetailed() {
        List<Map<String, Object>> lessons = List.of(
                Map.of("ID", 1L, "LESSON_NO", 1, "NAME", "Xuất phát",
                        "ANNOUNCE_TEXT", "Bài thi xuất phát", "DURATION_SECONDS", 20));
        Map<Long, List<Map<String, Object>>> steps = Map.of(1L, List.of(
                Map.of("ID", 1L, "STEP_NO", 1, "NAME", "Thắt dây an toàn",
                        "ANNOUNCE_TEXT", "Thắt dây an toàn", "DURATION_SECONDS", 10, "ENABLED", 1),
                Map.of("ID", 2L, "STEP_NO", 2, "NAME", "Bật xi nhan trái và xuất phát",
                        "ANNOUNCE_TEXT", "Bật xi nhan trái, xuất phát", "DURATION_SECONDS", 20, "ENABLED", 1)));
        List<ExamEngine.PlanItem> plan = ExamEngine.buildPlan(lessons, steps, true, true);
        assertEquals(2, plan.size());
        assertEquals("Thắt dây an toàn", plan.get(0).name());
        // Flow 3: không bao giờ mở bước nhỏ.
        List<ExamEngine.PlanItem> plan3 = ExamEngine.buildPlan(lessons, steps, true, false);
        assertEquals(1, plan3.size());
        assertEquals("Xuất phát", plan3.get(0).name());
    }

    @Test
    void buildPlanFallsBackToLessonWhenStepsDisabled() {
        List<Map<String, Object>> lessons = List.of(
                Map.of("ID", 1L, "LESSON_NO", 1, "NAME", "Xuất phát",
                        "ANNOUNCE_TEXT", "Bài thi xuất phát", "DURATION_SECONDS", 20));
        List<ExamEngine.PlanItem> plan = ExamEngine.buildPlan(lessons, Map.of(), false, true);
        assertEquals(1, plan.size());
        assertEquals(20, plan.get(0).durationSeconds());
    }
}

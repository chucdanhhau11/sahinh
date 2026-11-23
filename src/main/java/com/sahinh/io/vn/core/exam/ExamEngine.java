package com.sahinh.io.vn.core.exam;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ExamEngine — engine DUY NHẤT chứa logic thi, dùng chung cho 3 luồng.
 * Không copy logic tính điểm ra 3 nơi. Không đọc DB ở đây (đọc DB là việc
 * của ExamService / mod); engine chỉ nhận dữ liệu đã load và tính toán.
 *
 * Quy tắc đã chốt:
 * - Bắt đầu 100 điểm, đạt từ 80.
 * - Lỗi thường: trừ DEDUCT_POINTS, đi tiếp.
 * - Lỗi liệt (IS_LIET=1): dừng ngay, RỚT.
 * - Luồng 1: hết giờ / sai ở bước nào -> dừng ngay, làm lại từ đầu
 *   (không trừ điểm rồi đi tiếp). Quyết định này do caller (Flow 1)
 *   gọi failStrict(); engine chỉ cung cấp hàm đó.
 */
public class ExamEngine {
    public static final int START_SCORE = 100;
    public static final int PASS_SCORE = 80;
    /** Tổng thời gian chuẩn 18 phút = 1080 giây. Chỉ áp cho Luồng 2/3. */
    public static final int TOTAL_LIMIT_SECONDS = 1080;

    /**
     * Quá tổng thời gian: cứ 3 giây trừ 1 điểm (Luồng 2/3, server-side).
     * elapsedSeconds <= 1080 -> 0. Luồng 1 KHÔNG áp dụng (giây chỉnh tự do).
     */
    public static int overtimeDeduct(long elapsedSeconds) {
        if (elapsedSeconds <= TOTAL_LIMIT_SECONDS) {
            return 0;
        }
        return (int) ((elapsedSeconds - TOTAL_LIMIT_SECONDS) / 3);
    }

    /** Số tự động thì bỏ bài 9 (Thay đổi số trên đường bằng). Mặc định số sàn. */
    public static boolean isAutoTransmission(String transmission) {
        return "AUTO".equalsIgnoreCase(transmission);
    }

    /** Một mục trong kế hoạch thi: 1 bước nhỏ (Flow 1/2) hoặc 1 bài (Flow 3). */
    public record PlanItem(int lessonNo, Integer stepNo, String name, String announce,
                           int durationSeconds, long lessonId, Long stepId) {
    }

    public record ApplyResult(int score, boolean isLiet, String failedReason, boolean finished) {
    }

    public static boolean isPass(int score) {
        return score >= PASS_SCORE;
    }

    /**
     * Xây kế hoạch thi từ lessons + steps đã load từ DB.
     * useSteps=true (Flow 1/2): nếu detailedEnabled thì mở rộng thành bước nhỏ
     * (chỉ bước ENABLED=1); nếu tắt thì dùng bài cha. useSteps=false (Flow 3):
     * luôn dùng bài cha, không bao giờ mở bước nhỏ.
     */
    public static List<PlanItem> buildPlan(List<Map<String, Object>> lessons,
                                           Map<Long, List<Map<String, Object>>> stepsByLesson,
                                           boolean detailedEnabled, boolean useSteps) {
        return buildPlan(lessons, stepsByLesson, detailedEnabled, useSteps, "MANUAL");
    }

    /**
     * Như buildPlan 4 tham số, thêm hộp số: transmission="AUTO" thì bỏ
     * bài 9 khỏi kế hoạch (plan, tiến trình, chấm điểm đều bỏ nhất quán).
     */
    public static List<PlanItem> buildPlan(List<Map<String, Object>> lessons,
                                           Map<Long, List<Map<String, Object>>> stepsByLesson,
                                           boolean detailedEnabled, boolean useSteps,
                                           String transmission) {
        List<PlanItem> plan = new ArrayList<>();
        boolean auto = isAutoTransmission(transmission);
        for (Map<String, Object> l : lessons) {
            int lessonNo = ((Number) l.get("LESSON_NO")).intValue();
            if (auto && lessonNo == 9) {
                continue; // Số tự động không thi bài 9.
            }
            long lessonId = ((Number) l.get("ID")).longValue();
            String name = String.valueOf(l.get("NAME"));
            String announce = String.valueOf(l.get("ANNOUNCE_TEXT"));
            int duration = l.get("DURATION_SECONDS") == null ? 20
                    : ((Number) l.get("DURATION_SECONDS")).intValue();
            if (useSteps && detailedEnabled) {
                List<Map<String, Object>> steps = stepsByLesson.getOrDefault(lessonId, List.of());
                boolean anyEnabled = false;
                for (Map<String, Object> s : steps) {
                    Object en = s.get("ENABLED");
                    boolean enabled = en == null || ((Number) en).intValue() == 1;
                    if (!enabled) {
                        continue;
                    }
                    anyEnabled = true;
                    plan.add(new PlanItem(lessonNo,
                            ((Number) s.get("STEP_NO")).intValue(),
                            String.valueOf(s.get("NAME")),
                            String.valueOf(s.get("ANNOUNCE_TEXT")),
                            s.get("DURATION_SECONDS") == null ? duration
                                    : ((Number) s.get("DURATION_SECONDS")).intValue(),
                            lessonId, ((Number) s.get("ID")).longValue()));
                }
                if (!anyEnabled) {
                    plan.add(new PlanItem(lessonNo, null, name, announce, duration, lessonId, null));
                }
            } else {
                plan.add(new PlanItem(lessonNo, null, name, announce, duration, lessonId, null));
            }
        }
        return plan;
    }

    /** Áp lỗi vào điểm hiện tại. Trả về điểm mới + có liệt không. */
    public static ApplyResult applyError(int score, Map<String, Object> rule) {
        if (rule == null) {
            return new ApplyResult(score, false, null, false);
        }
        Object liet = rule.get("IS_LIET");
        boolean isLiet = liet != null && ((Number) liet).intValue() == 1;
        String name = String.valueOf(rule.get("NAME"));
        if (isLiet) {
            return new ApplyResult(score, true, "RỚT do lỗi liệt: " + name, true);
        }
        int deduct = rule.get("DEDUCT_POINTS") == null ? 0
                : ((Number) rule.get("DEDUCT_POINTS")).intValue();
        return new ApplyResult(Math.max(0, score - deduct), false, null, false);
    }

    /** Luồng 1: sai / hết giờ ở bước nào -> dừng ngay, điểm về 0, làm lại. */
    public static ApplyResult failStrict(String stepLabel) {
        return new ApplyResult(0, true, "Trượt ở " + stepLabel + " — làm lại từ đầu", true);
    }
}

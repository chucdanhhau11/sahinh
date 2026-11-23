package com.sahinh.io.vn.core.exam;

import com.sahinh.io.vn.core.dataset.DataSetRegistry;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * ExamService — service thi dùng chung cho SHA0_0400/0500/0600.
 * Đọc DB qua DataSetRegistry, tính điểm qua ExamEngine, ghi SH_SESSION /
 * SH_SESSION_LOG. Các mod của 3 luồng chỉ là lớp mỏng gọi vào đây.
 */
@Service
public class ExamService {
    private final DataSetRegistry registry;

    public ExamService(DataSetRegistry registry) {
        this.registry = registry;
    }

    private Map<String, Object> row(Object... kv) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            m.put(String.valueOf(kv[i]), kv[i + 1]);
        }
        return m;
    }

    /** Preset đang áp dụng của user; fallback preset mẫu id=1. */
    public Map<String, Object> activePreset(long userId) {
        Map<String, Object> u = registry.get("sahinh").findOne(
                "SELECT ACTIVE_PRESET_ID FROM SYS_USER WHERE ID=?", userId);
        Long pid = null;
        if (u != null && u.get("ACTIVE_PRESET_ID") != null) {
            pid = ((Number) u.get("ACTIVE_PRESET_ID")).longValue();
        }
        if (pid != null) {
            Map<String, Object> p = registry.get("sahinh").findOne(
                    "SELECT * FROM SH_PRESET WHERE ID=?", pid);
            if (p != null) {
                return p;
            }
        }
        return registry.get("sahinh").findOne(
                "SELECT * FROM SH_PRESET WHERE IS_TEMPLATE=1 ORDER BY ID LIMIT 1");
    }

    public List<Map<String, Object>> listLessons(long presetId) {
        return registry.get("sahinh").searchAndRetrieve(
                "SELECT * FROM SH_LESSON WHERE PRESET_ID=? ORDER BY SORT_ORDER, LESSON_NO", presetId);
    }

    public Map<Long, List<Map<String, Object>>> stepsByLesson(long presetId) {
        List<Map<String, Object>> steps = registry.get("sahinh").searchAndRetrieve(
                "SELECT s.* FROM SH_STEP s JOIN SH_LESSON l ON l.ID=s.LESSON_ID "
                        + "WHERE l.PRESET_ID=? ORDER BY l.SORT_ORDER, s.SORT_ORDER", presetId);
        Map<Long, List<Map<String, Object>>> out = new LinkedHashMap<>();
        for (Map<String, Object> s : steps) {
            long lid = ((Number) s.get("LESSON_ID")).longValue();
            out.computeIfAbsent(lid, k -> new ArrayList<>()).add(s);
        }
        return out;
    }

    public List<Map<String, Object>> listErrors(long presetId, Integer lessonNo) {
        if (lessonNo == null) {
            return registry.get("sahinh").searchAndRetrieve(
                    "SELECT * FROM SH_ERROR_RULE WHERE PRESET_ID=? ORDER BY SORT_ORDER", presetId);
        }
        return registry.get("sahinh").searchAndRetrieve(
                "SELECT * FROM SH_ERROR_RULE WHERE PRESET_ID=? AND (LESSON_NO=? OR LESSON_NO=0) "
                        + "ORDER BY SORT_ORDER", presetId, lessonNo);
    }

    /** Kế hoạch thi (plan + lỗi theo bài) cho 1 luồng. */
    public Map<String, Object> planFor(long presetId, int flow) {
        return planFor(presetId, flow, "MANUAL");
    }

    public Map<String, Object> planFor(long presetId, int flow, String transmission) {
        Map<String, Object> preset = registry.get("sahinh").findOne(
                "SELECT * FROM SH_PRESET WHERE ID=?", presetId);
        boolean detailed = preset != null && preset.get("DETAILED_ENABLED") != null
                && ((Number) preset.get("DETAILED_ENABLED")).intValue() == 1;
        boolean useSteps = flow == 1 || flow == 2; // Flow 3: không bao giờ dùng bước nhỏ
        List<Map<String, Object>> lessons = listLessons(presetId);
        List<ExamEngine.PlanItem> plan = ExamEngine.buildPlan(
                lessons, stepsByLesson(presetId), detailed, useSteps, transmission);
        List<Map<String, Object>> items = new ArrayList<>();
        for (ExamEngine.PlanItem it : plan) {
            items.add(row("lessonNo", it.lessonNo(), "stepNo", it.stepNo(), "name", it.name(),
                    "announce", it.announce(), "durationSeconds", it.durationSeconds(),
                    "lessonId", it.lessonId(), "stepId", it.stepId()));
        }
        List<Map<String, Object>> errors = listErrors(presetId, null);
        Long dangerousRuleId = null;
        for (Map<String, Object> e : errors) {
            if ("DANGEROUS".equals(String.valueOf(e.get("CODE")))) {
                dangerousRuleId = ((Number) e.get("ID")).longValue();
            }
        }
        // Cột DANGEROUS_* do V4 thêm; DB chưa migrate thì coi như TẮT.
        boolean dangerous = preset != null && preset.get("DANGEROUS_ENABLED") != null
                && ((Number) preset.get("DANGEROUS_ENABLED")).intValue() == 1;
        int dangerousSeconds = preset != null && preset.get("DANGEROUS_TIMEOUT_SECONDS") != null
                ? ((Number) preset.get("DANGEROUS_TIMEOUT_SECONDS")).intValue() : 5;
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("presetId", presetId);
        out.put("flow", flow);
        out.put("transmission", ExamEngine.isAutoTransmission(transmission) ? "AUTO" : "MANUAL");
        out.put("detailedEnabled", detailed);
        out.put("items", items);
        out.put("errors", errors);
        out.put("dangerousEnabled", dangerous);
        out.put("dangerousTimeoutSeconds", dangerousSeconds);
        out.put("dangerousErrorRuleId", dangerousRuleId);
        return out;
    }

    public Map<String, Object> startSession(long userId, long presetId, int flow, String studentName) {
        return startSession(userId, presetId, flow, studentName, "MANUAL");
    }

    public Map<String, Object> startSession(long userId, long presetId, int flow,
                                            String studentName, String transmission) {
        String tr = ExamEngine.isAutoTransmission(transmission) ? "AUTO" : "MANUAL";
        long id = registry.get("sahinh").insertAndGetId(
                "INSERT INTO SH_SESSION (USER_ID, PRESET_ID, FLOW, TRANSMISSION, STUDENT_NAME, SCORE, STATUS, PASSED) "
                        + "VALUES (?,?,?,?,?,100,'RUNNING',0)",
                userId, presetId, flow, tr, studentName == null ? "" : studentName);
        return row("sessionId", id, "flow", flow, "presetId", presetId, "score", 100,
                "transmission", tr);
    }

    public Map<String, Object> getSession(long sessionId) {
        return registry.get("sahinh").findOne("SELECT * FROM SH_SESSION WHERE ID=?", sessionId);
    }

    /**
     * Chỉ chủ phiên thi hoặc SUPERADMIN mới được xem/ghi/kết thúc phiên đó.
     * Ngăn IDOR: đổi sessionId của người khác.
     */
    private void requireSessionAccess(Map<String, Object> session, Long userId, String role) {
        if (session == null) {
            throw new IllegalArgumentException("Phiên thi không tồn tại");
        }
        if ("SUPERADMIN".equals(role)) {
            return;
        }
        if (userId == null || session.get("USER_ID") == null
                || ((Number) session.get("USER_ID")).longValue() != userId.longValue()) {
            throw new IllegalArgumentException("Không có quyền với phiên thi này");
        }
    }

    public Map<String, Object> logEvent(long sessionId, Integer lessonNo, Integer stepNo,
                                        Long errorRuleId) {
        return logEvent(sessionId, lessonNo, stepNo, errorRuleId, null, "SUPERADMIN");
    }

    /**
     * Ghi 1 sự kiện chấm: errorRuleId=null nghĩa là đúng (PASS).
     * Trả về điểm mới + có liệt không. Điểm do server tính, frontend chỉ hiển thị.
     */
    public Map<String, Object> logEvent(long sessionId, Integer lessonNo, Integer stepNo,
                                        Long errorRuleId, Long userId, String role) {
        Map<String, Object> session = getSession(sessionId);
        requireSessionAccess(session, userId, role);
        if (!"RUNNING".equals(String.valueOf(session.get("STATUS")))) {
            throw new IllegalArgumentException("Phiên thi đã kết thúc");
        }
        int score = session.get("SCORE") == null ? 100 : ((Number) session.get("SCORE")).intValue();
        String result = "PASS";
        int deduct = 0;
        String note = null;
        boolean isLiet = false;
        if (errorRuleId != null) {
            Map<String, Object> rule = registry.get("sahinh").findOne(
                    "SELECT * FROM SH_ERROR_RULE WHERE ID=?", errorRuleId);
            ExamEngine.ApplyResult r = ExamEngine.applyError(score, rule);
            score = r.score();
            isLiet = r.isLiet();
            result = isLiet ? "LIET" : "FAIL";
            deduct = rule != null && rule.get("DEDUCT_POINTS") != null
                    ? ((Number) rule.get("DEDUCT_POINTS")).intValue() : 0;
            note = rule == null ? null : String.valueOf(rule.get("NAME"));
        }
        registry.get("sahinh").update(
                "INSERT INTO SH_SESSION_LOG (SESSION_ID, LESSON_NO, STEP_NO, RESULT, DEDUCT_POINTS, NOTE) "
                        + "VALUES (?,?,?,?,?,?)",
                sessionId, lessonNo, stepNo, result, deduct, note);
        if (isLiet) {
            registry.get("sahinh").update(
                    "UPDATE SH_SESSION SET SCORE=?, STATUS='ROT', PASSED=0, FAILED_STEP=?, "
                            + "FAILED_REASON=?, ENDED_AT=NOW() WHERE ID=?",
                    score, stepLabel(lessonNo, stepNo),
                    "RỚT do lỗi liệt: " + note, sessionId);
        } else {
            registry.get("sahinh").update(
                    "UPDATE SH_SESSION SET SCORE=? WHERE ID=?", score, sessionId);
        }
        return row("sessionId", sessionId, "score", score, "isLiet", isLiet,
                "passed", ExamEngine.isPass(score), "result", result, "deduct", deduct);
    }

    private String stepLabel(Integer lessonNo, Integer stepNo) {
        return "Bài " + lessonNo + (stepNo == null ? "" : " - bước " + stepNo);
    }

    public Map<String, Object> finishSession(long sessionId, boolean failed, String failedStep,
                                             String failedReason) {
        return finishSession(sessionId, failed, failedStep, failedReason, null, "SUPERADMIN");
    }

    /**
     * Kết thúc phiên. Flow 1: timeout -> failed=true, score=0 (failStrict).
     * Flow 2/3: score là điểm hiện tại, passed = score>=80, trừ thêm quá
     * tổng thời gian 18 phút (cứ 3 giây -1 điểm, tính từ STARTED_AT).
     * Sửa A1: phiên đã ROT thì GIỮ NGUYÊN ROT + điểm (không ghi đè FAILED/0).
     * Gọi finish 2 lần thì lần sau trả đúng trạng thái đã lưu (idempotent).
     */
    public Map<String, Object> finishSession(long sessionId, boolean failed, String failedStep,
                                             String failedReason, Long userId, String role) {
        Map<String, Object> session = getSession(sessionId);
        requireSessionAccess(session, userId, role);
        if (!"RUNNING".equals(String.valueOf(session.get("STATUS")))) {
            // Đã kết thúc rồi (kể cả ROT): trả đúng trạng thái đã lưu, không ghi đè.
            return finishResult(session);
        }
        int flow = session.get("FLOW") == null ? 1 : ((Number) session.get("FLOW")).intValue();
        int score = session.get("SCORE") == null ? 100 : ((Number) session.get("SCORE")).intValue();
        String status;
        boolean passed;
        if (failed) {
            score = 0;
            status = "FAILED";
            passed = false;
        } else {
            if (flow == 2 || flow == 3) {
                // Quá tổng thời gian 18 phút: cứ 3 giây -1 điểm (server-side).
                Map<String, Object> el = registry.get("sahinh").findOne(
                        "SELECT TIMESTAMPDIFF(SECOND, STARTED_AT, NOW()) AS ELAPSED "
                                + "FROM SH_SESSION WHERE ID=?", sessionId);
                long elapsed = el == null || el.get("ELAPSED") == null ? 0
                        : ((Number) el.get("ELAPSED")).longValue();
                int over = ExamEngine.overtimeDeduct(elapsed);
                if (over > 0) {
                    score = Math.max(0, score - over);
                    registry.get("sahinh").update(
                            "INSERT INTO SH_SESSION_LOG (SESSION_ID, LESSON_NO, STEP_NO, RESULT, "
                                    + "DEDUCT_POINTS, NOTE) VALUES (?,?,?,?,?,?)",
                            sessionId, 0, null, "OVERTIME", over,
                            "Quá tổng thời gian 18 phút: " + elapsed + " giây");
                }
            }
            passed = ExamEngine.isPass(score);
            status = passed ? "PASSED" : "FAILED";
        }
        registry.get("sahinh").update(
                "UPDATE SH_SESSION SET SCORE=?, STATUS=?, PASSED=?, FAILED_STEP=?, FAILED_REASON=?, "
                        + "ENDED_AT=NOW() WHERE ID=?",
                score, status, passed ? 1 : 0, failedStep, failedReason, sessionId);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("sessionId", sessionId);
        out.put("score", score);
        out.put("passed", passed);
        out.put("status", status);
        out.put("failedStep", failedStep);
        out.put("failedReason", failedReason);
        out.put("resultText", "ROT".equals(status) ? "RỚT DO LỖI LIỆT"
                : passed ? "ĐẠT" : "TRƯỢT");
        return out;
    }

    /** Trả kết quả từ 1 dòng SH_SESSION đã lưu (dùng khi finish gọi 2 lần). */
    private Map<String, Object> finishResult(Map<String, Object> session) {
        String status = String.valueOf(session.get("STATUS"));
        boolean passed = session.get("PASSED") != null
                && ((Number) session.get("PASSED")).intValue() == 1;
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("sessionId", ((Number) session.get("ID")).longValue());
        out.put("score", session.get("SCORE") == null ? 0
                : ((Number) session.get("SCORE")).intValue());
        out.put("passed", passed);
        out.put("status", status);
        out.put("failedStep", session.get("FAILED_STEP"));
        out.put("failedReason", session.get("FAILED_REASON"));
        out.put("resultText", "ROT".equals(status) ? "RỚT DO LỖI LIỆT"
                : passed ? "ĐẠT" : "TRƯỢT");
        return out;
    }

    public Map<String, Object> resultOf(long sessionId) {
        return resultOf(sessionId, null, "SUPERADMIN");
    }

    public Map<String, Object> resultOf(long sessionId, Long userId, String role) {
        Map<String, Object> s = getSession(sessionId);
        if (s == null) {
            return null;
        }
        try {
            requireSessionAccess(s, userId, role);
        } catch (IllegalArgumentException e) {
            return null; // Không có quyền: trả 404 như không tồn tại, không lộ phiên.
        }
        List<Map<String, Object>> logs = registry.get("sahinh").searchAndRetrieve(
                "SELECT * FROM SH_SESSION_LOG WHERE SESSION_ID=? ORDER BY ID", sessionId);
        Map<String, Object> out = new LinkedHashMap<>(s);
        out.put("logs", logs);
        return out;
    }

    public List<Map<String, Object>> listHistory(Long userId, String role) {
        if ("SUPERADMIN".equals(role)) {
            return registry.get("sahinh").searchAndRetrieve(
                    "SELECT s.*, u.USERNAME FROM SH_SESSION s LEFT JOIN SYS_USER u ON u.ID=s.USER_ID "
                            + "ORDER BY s.ID DESC LIMIT 200");
        }
        return registry.get("sahinh").searchAndRetrieve(
                "SELECT s.*, u.USERNAME FROM SH_SESSION s LEFT JOIN SYS_USER u ON u.ID=s.USER_ID "
                        + "WHERE s.USER_ID=? ORDER BY s.ID DESC LIMIT 200", userId);
    }

    public Map<String, Object> dashboardStats(Long userId, String role) {
        String where = "SUPERADMIN".equals(role) ? "" : "WHERE USER_ID=" + userId;
        int total = registry.get("sahinh").count("SELECT COUNT(*) FROM SH_SESSION " + where);
        int passed = registry.get("sahinh").count(
                "SELECT COUNT(*) FROM SH_SESSION " + where + ("".equals(where) ? " WHERE " : " AND ")
                        + "PASSED=1");
        int rot = registry.get("sahinh").count(
                "SELECT COUNT(*) FROM SH_SESSION " + where + ("".equals(where) ? " WHERE " : " AND ")
                        + "STATUS='ROT'");
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("totalSessions", total);
        out.put("passedSessions", passed);
        out.put("rotSessions", rot);
        out.put("passRate", total == 0 ? 0 : Math.round(passed * 100.0 / total));
        return out;
    }
}

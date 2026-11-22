package com.sahinh.io.vn.api.SH.A0.SHA0_0100;

import com.sahinh.io.vn.core.dataset.DataSetRegistry;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/** mod — toàn bộ nghiệp vụ + SQL qua DataSetRegistry. */
@Component
public class SHA0_0100_mod {
    protected final DataSetRegistry registry;

    public SHA0_0100_mod(DataSetRegistry registry) {
        this.registry = registry;
    }

    /** Chỉ Superadmin hoặc chủ sở hữu bản preset riêng mới được sửa. */
    public boolean canEditPreset(long presetId, long userId, String role) {
        if ("SUPERADMIN".equals(role)) {
            return true;
        }
        Map<String, Object> p = registry.get("sahinh").findOne(
                "SELECT * FROM SH_PRESET WHERE ID=?", presetId);
        if (p == null) {
            return false;
        }
        Object tmpl = p.get("IS_TEMPLATE");
        if (tmpl != null && ((Number) tmpl).intValue() == 1) {
            return false; // preset mẫu của Superadmin: thầy phải clone (Tự setup riêng)
        }
        return p.get("OWNER_ID") != null && ((Number) p.get("OWNER_ID")).longValue() == userId;
    }

    private void requireEdit(long presetId, long userId, String role) {
        if (!canEditPreset(presetId, userId, role)) {
            throw new IllegalArgumentException(
                    "Preset mẫu của Superadmin không được sửa trực tiếp — hãy bấm Tự setup riêng để copy ra bản riêng");
        }
    }

    public List<Map<String, Object>> listLessons(long presetId) {
        return registry.get("sahinh").searchAndRetrieve(
                "SELECT * FROM SH_LESSON WHERE PRESET_ID=? ORDER BY SORT_ORDER, LESSON_NO", presetId);
    }

    public List<Map<String, Object>> listSteps(long lessonId) {
        return registry.get("sahinh").searchAndRetrieve(
                "SELECT * FROM SH_STEP WHERE LESSON_ID=? ORDER BY SORT_ORDER, STEP_NO", lessonId);
    }

    public void updateLesson(long id, String announce, Integer duration,
                             long userId, String role) {
        Map<String, Object> l = registry.get("sahinh").findOne(
                "SELECT PRESET_ID FROM SH_LESSON WHERE ID=?", id);
        if (l == null) {
            throw new IllegalArgumentException("Bài thi không tồn tại");
        }
        requireEdit(((Number) l.get("PRESET_ID")).longValue(), userId, role);
        // Luồng 1: giây chỉnh tự do, KHÔNG validate tổng = 18 phút, KHÔNG ép luật.
        registry.get("sahinh").update(
                "UPDATE SH_LESSON SET ANNOUNCE_TEXT=?, DURATION_SECONDS=? WHERE ID=?",
                announce, duration, id);
    }

    public void updateStep(long id, String announce, Integer duration, Integer enabled,
                           long userId, String role) {
        Map<String, Object> s = registry.get("sahinh").findOne(
                "SELECT l.PRESET_ID FROM SH_STEP s JOIN SH_LESSON l ON l.ID=s.LESSON_ID WHERE s.ID=?", id);
        if (s == null) {
            throw new IllegalArgumentException("Bước nhỏ không tồn tại");
        }
        requireEdit(((Number) s.get("PRESET_ID")).longValue(), userId, role);
        registry.get("sahinh").update(
                "UPDATE SH_STEP SET ANNOUNCE_TEXT=?, DURATION_SECONDS=?, ENABLED=? WHERE ID=?",
                announce, duration, enabled, id);
    }

    public void setDetailed(long presetId, boolean detailed, long userId, String role) {
        requireEdit(presetId, userId, role);
        registry.get("sahinh").update(
                "UPDATE SH_PRESET SET DETAILED_ENABLED=? WHERE ID=?", detailed ? 1 : 0, presetId);
    }
}

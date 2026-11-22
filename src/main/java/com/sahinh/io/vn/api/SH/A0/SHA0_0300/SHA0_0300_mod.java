package com.sahinh.io.vn.api.SH.A0.SHA0_0300;

import com.sahinh.io.vn.core.dataset.DataSetRegistry;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/** mod — toàn bộ nghiệp vụ + SQL qua DataSetRegistry. */
@Component
public class SHA0_0300_mod {
    protected final DataSetRegistry registry;

    public SHA0_0300_mod(DataSetRegistry registry) {
        this.registry = registry;
    }

    public List<Map<String, Object>> listPresets(long userId, String role) {
        if ("SUPERADMIN".equals(role)) {
            return registry.get("sahinh").searchAndRetrieve(
                    "SELECT p.*, u.USERNAME AS OWNER_NAME FROM SH_PRESET p "
                            + "LEFT JOIN SYS_USER u ON u.ID=p.OWNER_ID ORDER BY p.ID");
        }
        return registry.get("sahinh").searchAndRetrieve(
                "SELECT p.*, u.USERNAME AS OWNER_NAME FROM SH_PRESET p "
                        + "LEFT JOIN SYS_USER u ON u.ID=p.OWNER_ID "
                        + "WHERE p.IS_TEMPLATE=1 OR p.OWNER_ID=? ORDER BY p.ID", userId);
    }

    /** Thầy bấm Áp dụng: chỉ đổi preset đang dùng, không sửa preset gốc. */
    public void applyPreset(long presetId, long userId) {
        Map<String, Object> p = registry.get("sahinh").findOne(
                "SELECT ID FROM SH_PRESET WHERE ID=?", presetId);
        if (p == null) {
            throw new IllegalArgumentException("Preset không tồn tại");
        }
        registry.get("sahinh").update(
                "UPDATE SYS_USER SET ACTIVE_PRESET_ID=? WHERE ID=?", presetId, userId);
    }

    /**
     * Tự setup riêng: COPY toàn bộ preset (bài + bước + lỗi) ra bản riêng
     * của thầy. KHÔNG BAO GIỜ ghi đè bản gốc của Superadmin.
     */
    public long clonePreset(long sourcePresetId, long userId, String newName) {
        Map<String, Object> src = registry.get("sahinh").findOne(
                "SELECT * FROM SH_PRESET WHERE ID=?", sourcePresetId);
        if (src == null) {
            throw new IllegalArgumentException("Preset gốc không tồn tại");
        }
        Object det = src.get("DETAILED_ENABLED");
        long newId = registry.get("sahinh").insertAndGetId(
                "INSERT INTO SH_PRESET (NAME, OWNER_ID, IS_TEMPLATE, DETAILED_ENABLED, IS_ACTIVE) "
                        + "VALUES (?,?,0,?,1)",
                newName == null || newName.isBlank()
                        ? String.valueOf(src.get("NAME")) + " (bản riêng)"
                        : newName,
                userId, det == null ? 1 : ((Number) det).intValue());
        copyPresetContent(sourcePresetId, newId);
        return newId;
    }

    /** Superadmin tạo preset mới: copy nội dung từ preset mẫu id=1. */
    public long createPreset(String name, long userId) {
        long newId = registry.get("sahinh").insertAndGetId(
                "INSERT INTO SH_PRESET (NAME, OWNER_ID, IS_TEMPLATE, DETAILED_ENABLED, IS_ACTIVE) "
                        + "VALUES (?,?,1,1,1)",
                name, userId);
        copyPresetContent(1L, newId);
        return newId;
    }

    private void copyPresetContent(long sourcePresetId, long targetPresetId) {
        List<Map<String, Object>> lessons = registry.get("sahinh").searchAndRetrieve(
                "SELECT * FROM SH_LESSON WHERE PRESET_ID=? ORDER BY SORT_ORDER", sourcePresetId);
        for (Map<String, Object> l : lessons) {
            long newLessonId = registry.get("sahinh").insertAndGetId(
                    "INSERT INTO SH_LESSON (PRESET_ID, LESSON_NO, NAME, ANNOUNCE_TEXT, "
                            + "DURATION_SECONDS, SORT_ORDER) VALUES (?,?,?,?,?,?)",
                    targetPresetId, l.get("LESSON_NO"), l.get("NAME"), l.get("ANNOUNCE_TEXT"),
                    l.get("DURATION_SECONDS"), l.get("SORT_ORDER"));
            List<Map<String, Object>> steps = registry.get("sahinh").searchAndRetrieve(
                    "SELECT * FROM SH_STEP WHERE LESSON_ID=? ORDER BY SORT_ORDER",
                    ((Number) l.get("ID")).longValue());
            for (Map<String, Object> s : steps) {
                registry.get("sahinh").update(
                        "INSERT INTO SH_STEP (LESSON_ID, STEP_NO, NAME, ANNOUNCE_TEXT, "
                                + "DURATION_SECONDS, ENABLED, SORT_ORDER) VALUES (?,?,?,?,?,?,?)",
                        newLessonId, s.get("STEP_NO"), s.get("NAME"), s.get("ANNOUNCE_TEXT"),
                        s.get("DURATION_SECONDS"), s.get("ENABLED"), s.get("SORT_ORDER"));
            }
        }
        List<Map<String, Object>> errors = registry.get("sahinh").searchAndRetrieve(
                "SELECT * FROM SH_ERROR_RULE WHERE PRESET_ID=? ORDER BY SORT_ORDER", sourcePresetId);
        for (Map<String, Object> e : errors) {
            registry.get("sahinh").update(
                    "INSERT INTO SH_ERROR_RULE (PRESET_ID, LESSON_NO, CODE, NAME, DEDUCT_POINTS, "
                            + "IS_LIET, SORT_ORDER) VALUES (?,?,?,?,?,?,?)",
                    targetPresetId, e.get("LESSON_NO"), e.get("CODE"), e.get("NAME"),
                    e.get("DEDUCT_POINTS"), e.get("IS_LIET"), e.get("SORT_ORDER"));
        }
    }
}

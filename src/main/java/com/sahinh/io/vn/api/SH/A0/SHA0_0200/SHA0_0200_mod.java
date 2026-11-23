package com.sahinh.io.vn.api.SH.A0.SHA0_0200;

import com.sahinh.io.vn.core.dataset.DataSetRegistry;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/** mod — toàn bộ nghiệp vụ + SQL qua DataSetRegistry. */
@Component
public class SHA0_0200_mod {
    protected final DataSetRegistry registry;

    public SHA0_0200_mod(DataSetRegistry registry) {
        this.registry = registry;
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

    /** S07-A4: tạo lỗi mới trong preset (kiểm tra quyền sở hữu preset). */
    public long createError(long presetId, Integer lessonNo, String name,
                            Integer deduct, Integer isLiet, long userId, String role) {
        com.sahinh.io.vn.api.SH.A0.SHA0_0100.SHA0_0100_mod cfg =
                new com.sahinh.io.vn.api.SH.A0.SHA0_0100.SHA0_0100_mod(registry);
        if (!cfg.canEditPreset(presetId, userId, role)) {
            throw new IllegalArgumentException(
                    "Preset mẫu của Superadmin không được sửa trực tiếp — hãy bấm Tự setup riêng");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Thiếu tên lỗi");
        }
        int ln = lessonNo == null ? 0 : lessonNo.intValue();
        int sort = 100;
        Map<String, Object> mx = registry.get("sahinh").findOne(
                "SELECT MAX(SORT_ORDER) AS M FROM SH_ERROR_RULE WHERE PRESET_ID=?", presetId);
        if (mx != null && mx.get("M") != null) {
            sort = ((Number) mx.get("M")).intValue() + 1;
        }
        return registry.get("sahinh").insertAndGetId(
                "INSERT INTO SH_ERROR_RULE (PRESET_ID, LESSON_NO, CODE, NAME, DEDUCT_POINTS, "
                        + "IS_LIET, SORT_ORDER) VALUES (?,?,?,?,?,?,?)",
                presetId, ln, "CUSTOM_" + System.currentTimeMillis(),
                name.trim(), deduct == null ? 0 : deduct, isLiet == null ? 0 : isLiet, sort);
    }

    /** S07-A4: xóa lỗi (kiểm tra quyền sở hữu preset). */
    public void deleteError(long id, long userId, String role) {
        Map<String, Object> e = registry.get("sahinh").findOne(
                "SELECT PRESET_ID FROM SH_ERROR_RULE WHERE ID=?", id);
        if (e == null) {
            throw new IllegalArgumentException("Lỗi không tồn tại");
        }
        com.sahinh.io.vn.api.SH.A0.SHA0_0100.SHA0_0100_mod cfg =
                new com.sahinh.io.vn.api.SH.A0.SHA0_0100.SHA0_0100_mod(registry);
        if (!cfg.canEditPreset(((Number) e.get("PRESET_ID")).longValue(), userId, role)) {
            throw new IllegalArgumentException(
                    "Preset mẫu của Superadmin không được sửa trực tiếp — hãy bấm Tự setup riêng");
        }
        registry.get("sahinh").update("DELETE FROM SH_ERROR_RULE WHERE ID=?", id);
    }

    public void updateError(long id, Integer deduct, Integer isLiet, long userId, String role) {
        Map<String, Object> e = registry.get("sahinh").findOne(
                "SELECT PRESET_ID FROM SH_ERROR_RULE WHERE ID=?", id);
        if (e == null) {
            throw new IllegalArgumentException("Lỗi không tồn tại");
        }
        long presetId = ((Number) e.get("PRESET_ID")).longValue();
        // Dùng chung luật sở hữu preset với SHA0_0100.
        com.sahinh.io.vn.api.SH.A0.SHA0_0100.SHA0_0100_mod cfg = new com.sahinh.io.vn.api.SH.A0.SHA0_0100.SHA0_0100_mod(registry);
        if (!cfg.canEditPreset(presetId, userId, role)) {
            throw new IllegalArgumentException(
                    "Preset mẫu của Superadmin không được sửa trực tiếp — hãy bấm Tự setup riêng");
        }
        registry.get("sahinh").update(
                "UPDATE SH_ERROR_RULE SET DEDUCT_POINTS=?, IS_LIET=? WHERE ID=?",
                deduct, isLiet, id);
    }
}

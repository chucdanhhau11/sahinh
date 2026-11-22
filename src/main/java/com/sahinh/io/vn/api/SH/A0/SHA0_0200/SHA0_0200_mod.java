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

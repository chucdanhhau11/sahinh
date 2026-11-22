package com.sahinh.io.vn.api.MS.A0.MSA0_0100;

import com.sahinh.io.vn.core.dataset.DataSetRegistry;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/** mod — toàn bộ nghiệp vụ + SQL qua DataSetRegistry. */
@Component
public class MSA0_0100_mod {
    protected final DataSetRegistry registry;

    public MSA0_0100_mod(DataSetRegistry registry) {
        this.registry = registry;
    }

    public Map<String, Object> stats(long userId, String role) {
        return new com.sahinh.io.vn.core.exam.ExamService(registry).dashboardStats(userId, role);
    }

    public List<Map<String, Object>> recent(long userId, String role) {
        List<Map<String, Object>> all =
                new com.sahinh.io.vn.core.exam.ExamService(registry).listHistory(userId, role);
        return all.subList(0, Math.min(5, all.size()));
    }
}

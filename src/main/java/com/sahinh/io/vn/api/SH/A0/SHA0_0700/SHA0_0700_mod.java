package com.sahinh.io.vn.api.SH.A0.SHA0_0700;

import com.sahinh.io.vn.core.dataset.DataSetRegistry;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/** mod — toàn bộ nghiệp vụ + SQL qua DataSetRegistry. */
@Component
public class SHA0_0700_mod {
    protected final DataSetRegistry registry;

    public SHA0_0700_mod(DataSetRegistry registry) {
        this.registry = registry;
    }

    public Map<String, Object> result(long sessionId) {
        return new com.sahinh.io.vn.core.exam.ExamService(registry).resultOf(sessionId);
    }

    public List<Map<String, Object>> history(long userId, String role) {
        return new com.sahinh.io.vn.core.exam.ExamService(registry).listHistory(userId, role);
    }
}

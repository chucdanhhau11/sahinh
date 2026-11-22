package com.sahinh.io.vn.api.ZZ.A0.ZZA0_0001;

import com.sahinh.io.vn.core.dataset.DataSetRegistry;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/** mod — toàn bộ nghiệp vụ + SQL qua DataSetRegistry. */
@Component
public class ZZA0_0001_mod {
    protected final DataSetRegistry registry;

    public ZZA0_0001_mod(DataSetRegistry registry) {
        this.registry = registry;
    }

    public Map<String, Object> login(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new IllegalArgumentException("Thiếu tên đăng nhập hoặc mật khẩu");
        }
        Map<String, Object> u = registry.get("sahinh").findOne(
                "SELECT * FROM SYS_USER WHERE USERNAME=? AND ACTIVE=1", username.trim());
        if (u == null || !com.sahinh.io.vn.core.security.PasswordUtil.matches(
                password, String.valueOf(u.get("PASSWORD_HASH")))) {
            throw new IllegalArgumentException("Tên đăng nhập hoặc mật khẩu không đúng");
        }
        // Không trả PASSWORD_HASH ra ngoài.
        u.remove("PASSWORD_HASH");
        return u;
    }
}

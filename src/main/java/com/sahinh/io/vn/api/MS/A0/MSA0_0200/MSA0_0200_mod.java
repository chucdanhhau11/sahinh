package com.sahinh.io.vn.api.MS.A0.MSA0_0200;

import com.sahinh.io.vn.core.dataset.DataSetRegistry;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/** mod — toàn bộ nghiệp vụ + SQL qua DataSetRegistry. */
@Component
public class MSA0_0200_mod {
    protected final DataSetRegistry registry;

    public MSA0_0200_mod(DataSetRegistry registry) {
        this.registry = registry;
    }

    public List<Map<String, Object>> listMenu(String role) {
        return registry.get("sahinh").searchAndRetrieve(
                "SELECT f.FUNCTION_ID, f.FUNCTION_NAME, p.PAGE_URL, f.FUN_LEVEL_SEQ "
                        + "FROM SYS_FUNCTION f "
                        + "JOIN SYS_FUNCTION_PAGE p ON p.FUNCTION_ID=f.FUNCTION_ID "
                        + "JOIN SYS_ROLE_FUNCTION rf ON rf.FUNCTION_ID=f.FUNCTION_ID "
                        + "WHERE rf.ROLE_CODE=? AND f.IS_MENU=1 ORDER BY f.FUN_LEVEL_SEQ",
                role);
    }

    public List<Map<String, Object>> listUsers() {
        return registry.get("sahinh").searchAndRetrieve(
                "SELECT ID, USERNAME, FULL_NAME, ROLE_CODE, ACTIVE, CREATED_AT FROM SYS_USER ORDER BY ID");
    }

    public long createUser(String username, String fullName, String role, String rawPassword) {
        if (username == null || username.isBlank() || rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Thiếu tên đăng nhập hoặc mật khẩu");
        }
        Map<String, Object> exists = registry.get("sahinh").findOne(
                "SELECT ID FROM SYS_USER WHERE USERNAME=?", username.trim());
        if (exists != null) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại");
        }
        return registry.get("sahinh").insertAndGetId(
                "INSERT INTO SYS_USER (USERNAME, PASSWORD_HASH, FULL_NAME, ROLE_CODE, ACTIVE) "
                        + "VALUES (?,?,?,?,1)",
                username.trim(),
                com.sahinh.io.vn.core.security.PasswordUtil.hash(rawPassword),
                fullName == null ? username.trim() : fullName, role);
    }
}

package com.sahinh.io.vn.core.security;

import jakarta.servlet.http.HttpSession;
import java.util.Map;

/** Đọc user đang đăng nhập từ HttpSession. Session key: SH_USER. */
public class AuthUtil {
    public static final String SESSION_KEY = "SH_USER";

    @SuppressWarnings("unchecked")
    public static Map<String, Object> currentUser(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object o = session.getAttribute(SESSION_KEY);
        return o instanceof Map ? (Map<String, Object>) o : null;
    }

    public static Long currentUserId(HttpSession session) {
        Map<String, Object> u = currentUser(session);
        if (u == null || u.get("ID") == null) {
            return null;
        }
        return ((Number) u.get("ID")).longValue();
    }

    public static String currentRole(HttpSession session) {
        Map<String, Object> u = currentUser(session);
        return u == null ? null : String.valueOf(u.get("ROLE_CODE"));
    }

    public static boolean isSuperAdmin(HttpSession session) {
        return "SUPERADMIN".equals(currentRole(session));
    }
}

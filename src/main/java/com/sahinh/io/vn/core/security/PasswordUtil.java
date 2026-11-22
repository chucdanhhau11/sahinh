package com.sahinh.io.vn.core.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/** BCrypt — chỉ dùng spring-security-crypto, không dùng starter-security. */
public class PasswordUtil {
    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder(10);

    public static String hash(String rawPassword) {
        return ENCODER.encode(rawPassword);
    }

    public static boolean matches(String rawPassword, String hash) {
        if (rawPassword == null || hash == null) {
            return false;
        }
        try {
            return ENCODER.matches(rawPassword, hash);
        } catch (Exception e) {
            return false;
        }
    }
}

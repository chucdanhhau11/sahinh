package com.sahinh.io.vn.api.MS.A0.MSA0_0200;

import com.sahinh.io.vn.core.ApiResponse;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** trx — REST API: nhận request, validate cơ bản, gọi mod, trả JSON. Không business. */
@RestController
@RequestMapping("/api/MS/A0/MSA0_0200")
public class MSA0_0200_trx {
    private final MSA0_0200_mod mod;

    public MSA0_0200_trx(MSA0_0200_mod mod) {
        this.mod = mod;
    }

    @GetMapping("/menu")
    public Map<String, Object> menu(HttpSession session) {
        try {
            return ApiResponse.ok(mod.listMenu(
                    com.sahinh.io.vn.core.security.AuthUtil.currentRole(session)));
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }

    @GetMapping("/users")
    public Map<String, Object> users(HttpSession session) {
        try {
            if (!com.sahinh.io.vn.core.security.AuthUtil.isSuperAdmin(session)) {
                return ApiResponse.error(403, "FORBIDDEN");
            }
            return ApiResponse.ok(mod.listUsers());
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }

    @PostMapping("/users")
    public Map<String, Object> createUser(@RequestBody Map<String, Object> body, HttpSession session) {
        try {
            if (!com.sahinh.io.vn.core.security.AuthUtil.isSuperAdmin(session)) {
                return ApiResponse.error(403, "FORBIDDEN");
            }
            long id = mod.createUser(
                    body.get("username") == null ? null : String.valueOf(body.get("username")),
                    body.get("fullName") == null ? null : String.valueOf(body.get("fullName")),
                    body.get("role") == null ? "TEACHER" : String.valueOf(body.get("role")),
                    body.get("password") == null ? null : String.valueOf(body.get("password")));
            return ApiResponse.ok(java.util.Map.of("id", id));
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }
}

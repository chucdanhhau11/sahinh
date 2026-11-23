package com.sahinh.io.vn.api.SH.A0.SHA0_0300;

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
@RequestMapping("/api/SH/A0/SHA0_0300")
public class SHA0_0300_trx {
    private final SHA0_0300_mod mod;

    public SHA0_0300_trx(SHA0_0300_mod mod) {
        this.mod = mod;
    }

    @GetMapping("/presets")
    public Map<String, Object> presets(HttpSession session) {
        try {
            return ApiResponse.ok(mod.listPresets(
                    com.sahinh.io.vn.core.security.AuthUtil.currentUserId(session),
                    com.sahinh.io.vn.core.security.AuthUtil.currentRole(session)));
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }

    @PostMapping("/apply")
    public Map<String, Object> apply(@RequestBody Map<String, Object> body, HttpSession session) {
        try {
            mod.applyPreset(((Number) body.get("presetId")).longValue(),
                    com.sahinh.io.vn.core.security.AuthUtil.currentUserId(session));
            return ApiResponse.ok("SUCCESS", null);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }

    @PostMapping("/clone")
    public Map<String, Object> clone(@RequestBody Map<String, Object> body, HttpSession session) {
        try {
            long id = mod.clonePreset(
                    ((Number) body.get("presetId")).longValue(),
                    com.sahinh.io.vn.core.security.AuthUtil.currentUserId(session),
                    body.get("name") == null ? null : String.valueOf(body.get("name")));
            return ApiResponse.ok(java.util.Map.of("id", id));
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }

    @PostMapping("/dangerous")
    public Map<String, Object> setDangerous(@RequestBody Map<String, Object> body, HttpSession session) {
        try {
            mod.setDangerous(
                    ((Number) body.get("presetId")).longValue(),
                    Boolean.TRUE.equals(body.get("enabled")),
                    body.get("timeoutSeconds") == null ? null
                            : ((Number) body.get("timeoutSeconds")).intValue(),
                    com.sahinh.io.vn.core.security.AuthUtil.currentUserId(session),
                    com.sahinh.io.vn.core.security.AuthUtil.currentRole(session));
            return ApiResponse.ok("SUCCESS", null);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }

    @PostMapping("/create")
    public Map<String, Object> create(@RequestBody Map<String, Object> body, HttpSession session) {
        try {
            if (!com.sahinh.io.vn.core.security.AuthUtil.isSuperAdmin(session)) {
                return ApiResponse.error(403, "FORBIDDEN");
            }
            long id = mod.createPreset(
                    body.get("name") == null ? "Preset mới" : String.valueOf(body.get("name")),
                    com.sahinh.io.vn.core.security.AuthUtil.currentUserId(session));
            return ApiResponse.ok(java.util.Map.of("id", id));
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }
}

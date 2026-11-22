package com.sahinh.io.vn.api.SH.A0.SHA0_0100;

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
@RequestMapping("/api/SH/A0/SHA0_0100")
public class SHA0_0100_trx {
    private final SHA0_0100_mod mod;

    public SHA0_0100_trx(SHA0_0100_mod mod) {
        this.mod = mod;
    }

    @GetMapping("/lessons")
    public Map<String, Object> lessons(@RequestParam long presetId) {
        try {
            return ApiResponse.ok(mod.listLessons(presetId));
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }

    @GetMapping("/steps")
    public Map<String, Object> steps(@RequestParam long lessonId) {
        try {
            return ApiResponse.ok(mod.listSteps(lessonId));
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }

    @PostMapping("/lesson")
    public Map<String, Object> updateLesson(@RequestBody Map<String, Object> body, HttpSession session) {
        try {
            mod.updateLesson(
                    ((Number) body.get("id")).longValue(),
                    body.get("announce") == null ? null : String.valueOf(body.get("announce")),
                    body.get("duration") == null ? null : ((Number) body.get("duration")).intValue(),
                    com.sahinh.io.vn.core.security.AuthUtil.currentUserId(session),
                    com.sahinh.io.vn.core.security.AuthUtil.currentRole(session));
            return ApiResponse.ok("SUCCESS", null);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }

    @PostMapping("/step")
    public Map<String, Object> updateStep(@RequestBody Map<String, Object> body, HttpSession session) {
        try {
            mod.updateStep(
                    ((Number) body.get("id")).longValue(),
                    body.get("announce") == null ? null : String.valueOf(body.get("announce")),
                    body.get("duration") == null ? null : ((Number) body.get("duration")).intValue(),
                    body.get("enabled") == null ? null : ((Number) body.get("enabled")).intValue(),
                    com.sahinh.io.vn.core.security.AuthUtil.currentUserId(session),
                    com.sahinh.io.vn.core.security.AuthUtil.currentRole(session));
            return ApiResponse.ok("SUCCESS", null);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }

    @PostMapping("/detail-toggle")
    public Map<String, Object> setDetailed(@RequestBody Map<String, Object> body, HttpSession session) {
        try {
            mod.setDetailed(
                    ((Number) body.get("presetId")).longValue(),
                    Boolean.TRUE.equals(body.get("detailed")),
                    com.sahinh.io.vn.core.security.AuthUtil.currentUserId(session),
                    com.sahinh.io.vn.core.security.AuthUtil.currentRole(session));
            return ApiResponse.ok("SUCCESS", null);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }
}

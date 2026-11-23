package com.sahinh.io.vn.api.SH.A0.SHA0_0200;

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
@RequestMapping("/api/SH/A0/SHA0_0200")
public class SHA0_0200_trx {
    private final SHA0_0200_mod mod;

    public SHA0_0200_trx(SHA0_0200_mod mod) {
        this.mod = mod;
    }

    @GetMapping("/errors")
    public Map<String, Object> errors(@RequestParam long presetId,
                                      @RequestParam(required = false) Integer lessonNo) {
        try {
            return ApiResponse.ok(mod.listErrors(presetId, lessonNo));
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }

    @PostMapping("/error/create")
    public Map<String, Object> createError(@RequestBody Map<String, Object> body, HttpSession session) {
        try {
            long id = mod.createError(
                    ((Number) body.get("presetId")).longValue(),
                    body.get("lessonNo") == null ? 0 : ((Number) body.get("lessonNo")).intValue(),
                    body.get("name") == null ? null : String.valueOf(body.get("name")),
                    body.get("deduct") == null ? 0 : ((Number) body.get("deduct")).intValue(),
                    body.get("isLiet") == null ? 0 : ((Number) body.get("isLiet")).intValue(),
                    com.sahinh.io.vn.core.security.AuthUtil.currentUserId(session),
                    com.sahinh.io.vn.core.security.AuthUtil.currentRole(session));
            return ApiResponse.ok(java.util.Map.of("id", id));
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }

    @PostMapping("/error/delete")
    public Map<String, Object> deleteError(@RequestBody Map<String, Object> body, HttpSession session) {
        try {
            mod.deleteError(
                    ((Number) body.get("id")).longValue(),
                    com.sahinh.io.vn.core.security.AuthUtil.currentUserId(session),
                    com.sahinh.io.vn.core.security.AuthUtil.currentRole(session));
            return ApiResponse.ok("SUCCESS", null);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }

    @PostMapping("/error")
    public Map<String, Object> updateError(@RequestBody Map<String, Object> body, HttpSession session) {
        try {
            mod.updateError(
                    ((Number) body.get("id")).longValue(),
                    body.get("deduct") == null ? null : ((Number) body.get("deduct")).intValue(),
                    body.get("isLiet") == null ? null : ((Number) body.get("isLiet")).intValue(),
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

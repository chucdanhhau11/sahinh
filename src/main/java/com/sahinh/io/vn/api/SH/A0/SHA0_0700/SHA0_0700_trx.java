package com.sahinh.io.vn.api.SH.A0.SHA0_0700;

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
@RequestMapping("/api/SH/A0/SHA0_0700")
public class SHA0_0700_trx {
    private final SHA0_0700_mod mod;

    public SHA0_0700_trx(SHA0_0700_mod mod) {
        this.mod = mod;
    }

    @GetMapping("/result")
    public Map<String, Object> result(@RequestParam long sessionId) {
        try {
            Map<String, Object> r = mod.result(sessionId);
            if (r == null) {
                return ApiResponse.error(404, "NOT_FOUND");
            }
            return ApiResponse.ok(r);
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }

    @GetMapping("/history")
    public Map<String, Object> history(HttpSession session) {
        try {
            return ApiResponse.ok(mod.history(
                    com.sahinh.io.vn.core.security.AuthUtil.currentUserId(session),
                    com.sahinh.io.vn.core.security.AuthUtil.currentRole(session)));
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }
}

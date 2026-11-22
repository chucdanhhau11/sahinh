package com.sahinh.io.vn.api.MS.A0.MSA0_0100;

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
@RequestMapping("/api/MS/A0/MSA0_0100")
public class MSA0_0100_trx {
    private final MSA0_0100_mod mod;

    public MSA0_0100_trx(MSA0_0100_mod mod) {
        this.mod = mod;
    }

    @GetMapping("/stats")
    public Map<String, Object> stats(HttpSession session) {
        try {
            return ApiResponse.ok(mod.stats(
                    com.sahinh.io.vn.core.security.AuthUtil.currentUserId(session),
                    com.sahinh.io.vn.core.security.AuthUtil.currentRole(session)));
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }

    @GetMapping("/recent")
    public Map<String, Object> recent(HttpSession session) {
        try {
            return ApiResponse.ok(mod.recent(
                    com.sahinh.io.vn.core.security.AuthUtil.currentUserId(session),
                    com.sahinh.io.vn.core.security.AuthUtil.currentRole(session)));
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }
}

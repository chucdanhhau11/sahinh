package com.sahinh.io.vn.api.ZZ.A0.ZZA0_0001;

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
@RequestMapping("/api/ZZ/A0/ZZA0_0001")
public class ZZA0_0001_trx {
    private final ZZA0_0001_mod mod;

    public ZZA0_0001_trx(ZZA0_0001_mod mod) {
        this.mod = mod;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, Object> body, HttpSession session) {
        try {
            Map<String, Object> user = mod.login(
                    body.get("username") == null ? null : String.valueOf(body.get("username")),
                    body.get("password") == null ? null : String.valueOf(body.get("password")));
            session.setAttribute("SH_USER", user);
            return ApiResponse.ok(user);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(401, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }

    @GetMapping("/session")
    public Map<String, Object> session(HttpSession session) {
        Map<String, Object> user = com.sahinh.io.vn.core.security.AuthUtil.currentUser(session);
        if (user == null) {
            return ApiResponse.error(401, "UNAUTHORIZED");
        }
        return ApiResponse.ok(user);
    }

    @PostMapping("/logout")
    public Map<String, Object> logout(HttpSession session) {
        session.invalidate();
        return ApiResponse.ok("SUCCESS", null);
    }
}

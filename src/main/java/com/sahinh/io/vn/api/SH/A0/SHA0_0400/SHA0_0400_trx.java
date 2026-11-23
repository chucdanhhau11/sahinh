package com.sahinh.io.vn.api.SH.A0.SHA0_0400;

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
@RequestMapping("/api/SH/A0/SHA0_0400")
public class SHA0_0400_trx {
    private final SHA0_0400_mod mod;

    public SHA0_0400_trx(SHA0_0400_mod mod) {
        this.mod = mod;
    }

    @GetMapping("/plan")
    public Map<String, Object> plan(@RequestParam long presetId,
                                      @RequestParam(defaultValue = "MANUAL") String transmission) {
        try {
            return ApiResponse.ok(mod.plan(presetId, transmission));
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }

    @PostMapping("/start")
    public Map<String, Object> start(@RequestBody Map<String, Object> body, HttpSession session) {
        try {
            return ApiResponse.ok(mod.start(
                    com.sahinh.io.vn.core.security.AuthUtil.currentUserId(session),
                    ((Number) body.get("presetId")).longValue(),
                    body.get("studentName") == null ? "" : String.valueOf(body.get("studentName")),
                    body.get("transmission") == null ? "MANUAL" : String.valueOf(body.get("transmission"))));
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }

    @PostMapping("/log")
    public Map<String, Object> log(@RequestBody Map<String, Object> body, HttpSession session) {
        try {
            Long errorRuleId = body.get("errorRuleId") == null ? null
                    : ((Number) body.get("errorRuleId")).longValue();
            Integer lessonNo = body.get("lessonNo") == null ? null
                    : ((Number) body.get("lessonNo")).intValue();
            Integer stepNo = body.get("stepNo") == null ? null
                    : ((Number) body.get("stepNo")).intValue();
            return ApiResponse.ok(mod.log(
                    ((Number) body.get("sessionId")).longValue(), lessonNo, stepNo, errorRuleId,
                    com.sahinh.io.vn.core.security.AuthUtil.currentUserId(session),
                    com.sahinh.io.vn.core.security.AuthUtil.currentRole(session)));
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }

    @PostMapping("/finish")
    public Map<String, Object> finish(@RequestBody Map<String, Object> body, HttpSession session) {
        try {
            return ApiResponse.ok(mod.finish(
                    ((Number) body.get("sessionId")).longValue(),
                    Boolean.TRUE.equals(body.get("failed")),
                    body.get("failedStep") == null ? null : String.valueOf(body.get("failedStep")),
                    body.get("failedReason") == null ? null : String.valueOf(body.get("failedReason")),
                    com.sahinh.io.vn.core.security.AuthUtil.currentUserId(session),
                    com.sahinh.io.vn.core.security.AuthUtil.currentRole(session)));
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }

    @GetMapping("/result")
    public Map<String, Object> result(@RequestParam long sessionId, HttpSession session) {
        try {
            Map<String, Object> r = mod.result(sessionId,
                    com.sahinh.io.vn.core.security.AuthUtil.currentUserId(session),
                    com.sahinh.io.vn.core.security.AuthUtil.currentRole(session));
            if (r == null) {
                return ApiResponse.error(404, "NOT_FOUND");
            }
            return ApiResponse.ok(r);
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }
}

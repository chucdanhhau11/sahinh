package com.sahinh.io.vn.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import com.sahinh.io.vn.core.dataset.DataSet;

/**
 * Quyền CHỈ kiểm tra ở Filter (theo Skills).
 * - Public: /, /login, /css, /js, /favicon, /actuator/health,
 *   /api/ZZ/.../login, /error.
 * - Còn lại: phải có session SH_USER. SUPERADMIN đi mọi nơi.
 * - Các role khác: tra SYS_FUNCTION_PAGE theo URL rồi kiểm tra
 *   SYS_ROLE_FUNCTION. Không có dòng quyền -> 403.
 * - Nếu DB chưa migrate xong thì chỉ cho qua trang login.
 */
@Component
@Order(1)
public class PermissionFilter implements Filter {
    private final DataSet ds;

    public PermissionFilter(DataSource dataSource) {
        this.ds = new DataSet(dataSource);
    }

    private boolean isPublic(String path) {
        return "/".equals(path) || "/login".equals(path) || "/error".equals(path)
                || path.startsWith("/css/") || path.startsWith("/js/")
                || path.startsWith("/images/") || "/favicon.ico".equals(path)
                || path.startsWith("/actuator/health")
                || path.equals("/api/ZZ/A0/ZZA0_0001/login")
                // S08: /session phải qua được Filter để trx trả JSON {code:401}
                // (HTTP 200) cho app.js xử lý êm. Trước đây Filter chặn bằng
                // HTTP 401 -> app.js chuyển về /login -> tải lại vô hạn.
                || path.equals("/api/ZZ/A0/ZZA0_0001/session")
                || path.equals("/api/ZZ/A0/ZZA0_0001/logout");
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String path = request.getRequestURI();
        if (isPublic(path)) {
            chain.doFilter(req, res);
            return;
        }
        HttpSession session = request.getSession(false);
        @SuppressWarnings("unchecked")
        Map<String, Object> user = session == null ? null
                : (Map<String, Object>) session.getAttribute("SH_USER");
        if (user == null) {
            deny(request, response, path);
            return;
        }
        String role = String.valueOf(user.get("ROLE_CODE"));
        if ("SUPERADMIN".equals(role)) {
            chain.doFilter(req, res);
            return;
        }
        try {
            // Tìm function theo URL trang hoặc tiền tố API.
            String functionId = null;
            Map<String, Object> page = ds.findOne(
                    "SELECT FUNCTION_ID FROM SYS_FUNCTION_PAGE WHERE PAGE_URL=?", path);
            if (page != null) {
                functionId = String.valueOf(page.get("FUNCTION_ID"));
            } else if (path.startsWith("/api/")) {
                // /api/SH/A0/SHA0_0400/... -> SHA00100...: bỏ dấu _ trong mã function.
                String[] parts = path.split("/");
                if (parts.length >= 5) {
                    functionId = parts[4].replace("_", "");
                }
            }
            if (functionId == null) {
                // S07-A8: fail-closed. Đường dẫn lạ, không có trong
                // SYS_FUNCTION_PAGE và không phải API đã biết -> chặn,
                // không cho qua như trước.
                forbid(request, response, path);
                return;
            }
            List<Map<String, Object>> rows = ds.searchAndRetrieve(
                    "SELECT 1 FROM SYS_ROLE_FUNCTION WHERE ROLE_CODE=? AND FUNCTION_ID=?",
                    role, functionId);
            if (rows.isEmpty()) {
                if (path.startsWith("/api/")) {
                    response.setStatus(403);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write(
                            "{\"code\":403,\"message\":\"FORBIDDEN\",\"data\":null}");
                } else {
                    response.sendRedirect("/login");
                }
                return;
            }
        } catch (Exception e) {
            // DB chưa sẵn sàng (chưa migrate): chỉ cho qua login/session check.
            if ("/api/ZZ/A0/ZZA0_0001/session".equals(path)) {
                chain.doFilter(req, res);
                return;
            }
            deny(request, response, path);
            return;
        }
        chain.doFilter(req, res);
    }

    /** Đã đăng nhập nhưng đường dẫn không có quyền / không tồn tại: 403. */
    private void forbid(HttpServletRequest request, HttpServletResponse response, String path)
            throws IOException {
        if (path.startsWith("/api/")) {
            response.setStatus(403);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":403,\"message\":\"FORBIDDEN\",\"data\":null}");
        } else {
            response.sendRedirect("/login");
        }
    }

    private void deny(HttpServletRequest request, HttpServletResponse response, String path)
            throws IOException {
        if (path.startsWith("/api/")) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"UNAUTHORIZED\",\"data\":null}");
        } else {
            response.sendRedirect("/login");
        }
    }
}

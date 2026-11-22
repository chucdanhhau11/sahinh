# S01 — Foundation + Auth + Docker (MS)

**Ngày:** 2026-11-23

Dashboard MSA0_0100: stats (tổng phiên, đạt, rớt liệt, tỉ lệ đậu) + phiên gần đây.
MSA0_0200: menu động từ SYS_FUNCTION/SYS_FUNCTION_PAGE/SYS_ROLE_FUNCTION, list users, tạo user (chỉ SUPERADMIN, BCrypt).

## Kiểm thử (sandbox — xem ROADMAP §10 S01–S06)

- `javac` trực tiếp toàn bộ `src/main/java` với classpath là toàn bộ jar trong `~/.m2/repository` (Maven qua proxy sandbox lỗi TLS, `mvn`/`docker` không có sẵn trong sandbox).
- Unit test `ExamEngineTest` chạy bằng junit-platform-console-standalone.
- Chưa verify được trong sandbox: `mvn test`, `mvn clean package`, `docker compose up --build`, chạy app thật + login + chạy tay 3 luồng. Cần máy dev có Docker/Maven.

# S06 — Kết quả & Lịch sử

**Ngày:** 2026-11-23

SHA0_0700: xem chi tiết phiên (điểm, ĐẠT/TRƯỢT/RỚT DO LỖI LIỆT, bước trượt, lý do, log từng lỗi) + lịch sử phiên (SUPERADMIN thấy tất cả, teacher/student thấy của mình). Flyway V3: SH_SESSION/SH_SESSION_LOG.

## Kiểm thử (sandbox — xem ROADMAP §10 S01–S06)

- `javac` trực tiếp toàn bộ `src/main/java` với classpath là toàn bộ jar trong `~/.m2/repository` (Maven qua proxy sandbox lỗi TLS, `mvn`/`docker` không có sẵn trong sandbox).
- Unit test `ExamEngineTest` chạy bằng junit-platform-console-standalone.
- Chưa verify được trong sandbox: `mvn test`, `mvn clean package`, `docker compose up --build`, chạy app thật + login + chạy tay 3 luồng. Cần máy dev có Docker/Maven.

# S04 — Thi Luồng 2 — Bấm chay chi tiết

**Ngày:** 2026-11-23

SHA0_0500: thầy bấm Next / + Đúng / - Sai (mở modal chọn lỗi cụ thể của bài đó); điểm do server tính qua POST /log (ExamService.logEvent); lỗi liệt -> dừng ngay, RỚT. Dùng bước nhỏ (~23).

## Kiểm thử (sandbox — xem ROADMAP §10 S01–S06)

- `javac` trực tiếp toàn bộ `src/main/java` với classpath là toàn bộ jar trong `~/.m2/repository` (Maven qua proxy sandbox lỗi TLS, `mvn`/`docker` không có sẵn trong sandbox).
- Unit test `ExamEngineTest` chạy bằng junit-platform-console-standalone.
- Chưa verify được trong sandbox: `mvn test`, `mvn clean package`, `docker compose up --build`, chạy app thật + login + chạy tay 3 luồng. Cần máy dev có Docker/Maven.

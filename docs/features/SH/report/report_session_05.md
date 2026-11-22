# S05 — Thi Luồng 3 — Bấm chay thi thật

**Ngày:** 2026-11-23

SHA0_0600: không bước nhỏ (ExamEngine.buildPlan useSteps=false), chỉ 11 bài, loa chỉ đọc tên bài; thầy phải bấm mới qua bài; cũng bấm + / -; không chỉnh giờ/điểm trong luồng này.

## Kiểm thử (sandbox — xem ROADMAP §10 S01–S06)

- `javac` trực tiếp toàn bộ `src/main/java` với classpath là toàn bộ jar trong `~/.m2/repository` (Maven qua proxy sandbox lỗi TLS, `mvn`/`docker` không có sẵn trong sandbox).
- Unit test `ExamEngineTest` chạy bằng junit-platform-console-standalone.
- Chưa verify được trong sandbox: `mvn test`, `mvn clean package`, `docker compose up --build`, chạy app thật + login + chạy tay 3 luồng. Cần máy dev có Docker/Maven.

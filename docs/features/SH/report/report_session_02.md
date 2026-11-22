# S02 — Cấu hình thi

**Ngày:** 2026-11-23

SHA0_0100: list/sửa bài (lời loa, giây — không validate tổng 18 phút) và bước nhỏ (lời loa, giây, bật/tắt); công tắc Hướng dẫn chi tiết.
SHA0_0200: list/sửa lỗi (điểm trừ, cờ liệt).
SHA0_0300: list preset, Áp dụng (chỉ đổi ACTIVE_PRESET_ID), Tự setup riêng (COPY toàn bộ bài+bước+lỗi ra bản riêng, không ghi đè gốc), Tạo preset mới (chỉ SUPERADMIN, copy từ mẫu id=1).
Flyway V2: SH_PRESET/SH_LESSON/SH_STEP/SH_ERROR_RULE, seed preset 'Chuẩn thi thật', 11 bài, 23 bước, 24 lỗi (Tắt máy trên dốc IS_LIET=1).

## Kiểm thử (sandbox — xem ROADMAP §10 S01–S06)

- `javac` trực tiếp toàn bộ `src/main/java` với classpath là toàn bộ jar trong `~/.m2/repository` (Maven qua proxy sandbox lỗi TLS, `mvn`/`docker` không có sẵn trong sandbox).
- Unit test `ExamEngineTest` chạy bằng junit-platform-console-standalone.
- Chưa verify được trong sandbox: `mvn test`, `mvn clean package`, `docker compose up --build`, chạy app thật + login + chạy tay 3 luồng. Cần máy dev có Docker/Maven.

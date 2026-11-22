# S03 — Thi Luồng 1 — Tự động

**Ngày:** 2026-11-23

ExamEngine + ExamService dùng chung: buildPlan (mở bước nhỏ khi detailed + Flow 1/2), applyError, failStrict, isPass>=80.
SHA0_0400: thầy chỉ bấm Bắt đầu 1 lần; exam.js tự đếm giờ, tự đọc loa (SpeechSynthesis vi-VN), tự chuyển bước; hết giờ ở bước nào -> POST /finish failed=true, điểm 0, hiện 'làm lại từ đầu'.

## Kiểm thử (sandbox — xem ROADMAP §10 S01–S06)

- `javac` trực tiếp toàn bộ `src/main/java` với classpath là toàn bộ jar trong `~/.m2/repository` (Maven qua proxy sandbox lỗi TLS, `mvn`/`docker` không có sẵn trong sandbox).
- Unit test `ExamEngineTest` chạy bằng junit-platform-console-standalone.
- Chưa verify được trong sandbox: `mvn test`, `mvn clean package`, `docker compose up --build`, chạy app thật + login + chạy tay 3 luồng. Cần máy dev có Docker/Maven.

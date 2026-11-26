# SaHinh-B2 — Mục lục dự án

> Điểm tra cứu nhanh. Trước khi chỉnh sửa mã, vẫn phải đọc theo thứ tự
> bắt buộc trong `ROADMAP.md` §1: Skills -> PROJECT_INDEX -> ROADMAP
> -> feature docs -> report -> migration/source.

## 1. Thứ tự đọc bắt buộc

| Thứ tự | Tệp/thư mục | Mục đích |
|---:|---|---|
| 1 | `skills/#SAHINH-B2—SKILLS.txt` | Quy ước gốc bất biến; luật tối cao. |
| 2 | `PROJECT_INDEX.md` | Bản đồ mã nguồn, module, migration, config (file này). |
| 3 | `ROADMAP.md` | Source of truth cấp project: kiến trúc, trạng thái, next task. |
| 4 | `docs/features/<FEATURE>/<FEATURE>.md` | Phạm vi và trạng thái feature. |
| 5 | `docs/features/<FEATURE>/report/` | Báo cáo từng session. |
| 6 | `src/main/resources/db/migration/` rồi source function | Bằng chứng schema và implementation thực tế. |
| 7 | `PROMPT.md` | Prompt chi tiết để agent/AI khác build lại app này. |

## 2. Bản đồ source code

| Nhóm | Đường dẫn | Quy ước |
|---|---|---|
| Boot application | `src/main/java/com/sahinh/io/vn/SaHinhApplication.java` | Điểm vào JAR/Embedded Tomcat. |
| Core | `src/main/java/com/sahinh/io/vn/core/` | `ApiResponse`, `core/dataset/` (`DataSetRegistry`/`DataSet` JDBC thuần), `core/security/` (BCrypt + AuthUtil session), `core/exam/` (`ExamEngine` + `ExamService` dùng chung 3 luồng). |
| Filter | `src/main/java/com/sahinh/io/vn/config/PermissionFilter.java` | Quyền chỉ kiểm tra ở Filter; SUPERADMIN đi mọi nơi; role khác tra `SYS_FUNCTION_PAGE` + `SYS_ROLE_FUNCTION`. |
| Controller | `src/main/java/com/sahinh/io/vn/controller/<SYSTEM>/<SUBSYSTEM>/<FUNCTION>/` | `*_pg.java`; chỉ mapping URL và trả template. |
| Transaction API | `src/main/java/com/sahinh/io/vn/api/<SYSTEM>/<SUBSYSTEM>/<FUNCTION>/` | `*_trx.java`; nhận request, validate cơ bản, gọi mod, trả JSON. |
| Business module | Cùng thư mục API | `*_mod.java`; toàn bộ business và SQL qua DataSetRegistry. |
| Thymeleaf template | `src/main/resources/templates/<SYSTEM>/<SUBSYSTEM>/<FUNCTION>/` | Mỗi function có template. |
| UI chung | `src/main/resources/static/css/app.css`, `static/js/app.js`, `static/js/exam.js` | `app.js`: session/menu động; `exam.js`: đồng hồ, loa SpeechSynthesis vi-VN, bíp Web Audio, nút Mute, logic 3 luồng (điểm do server tính). |
| Cấu hình | `src/main/resources/application.yml` | File YAML duy nhất. Datasource/Flyway qua env `DB_HOST/DB_PORT/DB_NAME/DB_USER/DB_PASSWORD`. |
| Migration | `src/main/resources/db/migration/` | V1 foundation/auth, V2 preset/bài/bước/lỗi + seed, V3 phiên thi/log, V4 hộp số + tình huống nguy hiểm (S07). |

## 3. Danh mục module

| System | Subsystem | Function | Chức năng | Trạng thái |
|---|---|---|---|---|
| `ZZ` | `A0` | `ZZA0_0001` | Đăng nhập/session (BCrypt, HttpSession `SH_USER`) | DONE — S01 |
| `MS` | `A0` | `MSA0_0100` | Dashboard (số phiên, tỉ lệ đậu, phiên gần đây) | DONE — S01 |
| `MS` | `A0` | `MSA0_0200` | Người dùng, role, menu động từ DB | DONE — S01 |
| `SH` | `A0` | `SHA0_0100` | Cấu hình Bài & Bước nhỏ (lời loa, giây, bật/tắt) | DONE — S02 |
| `SH` | `A0` | `SHA0_0200` | Cấu hình Lỗi & Điểm liệt (S07: thêm tạo + xóa lỗi) | DONE — S02, S07 |
| `SH` | `A0` | `SHA0_0300` | Preset: Áp dụng / Tự setup riêng (copy) / Tạo mới (S07: toggle tình huống nguy hiểm) | DONE — S02, S07 |
| `SH` | `A0` | `SHA0_0400` | Thi Luồng 1 — Tự động (S07: chọn hộp số, kiểm tra quyền phiên) | DONE — S03, S07 |
| `SH` | `A0` | `SHA0_0500` | Thi Luồng 2 — Bấm chay chi tiết (S07: chọn hộp số, kiểm tra quyền phiên, quá 18 phút trừ điểm server) | DONE — S04, S07 |
| `SH` | `A0` | `SHA0_0600` | Thi Luồng 3 — Bấm chay thi thật (S07: như Luồng 2) | DONE — S05, S07 |
| `SH` | `A0` | `SHA0_0700` | Kết quả & Lịch sử phiên thi (S07: kết quả kiểm tra quyền phiên) | DONE — S06, S07 |

## 4. URL trang theo mã function

| Function | URL HTML chuẩn |
|---|---|
| `ZZA0_0001` | `/` và `/login` |
| `MSA0_0100` | `/MS/MSA0_0100` |
| `MSA0_0200` | `/MS/MSA0_0200` |
| `SHA0_0100`–`SHA0_0700` | `/SH/SHA0_0100`–`/SH/SHA0_0700` |

API theo convention `/api/{SYSTEM_CODE}/{SUBSYSTEM_CODE}/{FUNCTION_CODE}/...`.

## 5. Database và cấu hình

| Nội dung | Vị trí | Ghi chú |
|---|---|---|
| Cấu hình ứng dụng, MySQL và Flyway | `src/main/resources/application.yml` | File YAML duy nhất; env `DB_HOST/DB_PORT/DB_NAME/DB_USER/DB_PASSWORD`. |
| Migration V1 | `db/migration/V1__create_foundation_auth.sql` | `SYS_USER/SYS_ROLE/SYS_ROLE_USER/SYS_FUNCTION/SYS_FUNCTION_PAGE/SYS_ROLE_FUNCTION`; seed admin/teacher/student (BCrypt). |
| Migration V2 | `db/migration/V2__create_exam_config.sql` | `SH_PRESET/SH_LESSON/SH_STEP/SH_ERROR_RULE`; seed preset "Chuẩn thi thật", 11 bài, 23 bước, 24 lỗi (trong đó "Tắt máy trên dốc" IS_LIET=1). |
| Migration V3 | `db/migration/V3__create_exam_session.sql` | `SH_SESSION/SH_SESSION_LOG`. |
| Migration V4 | `db/migration/V4__add_transmission_and_dangerous.sql` | `SH_SESSION.TRANSMISSION` (AUTO/MANUAL, mặc định MANUAL); `SH_PRESET.DANGEROUS_ENABLED` + `DANGEROUS_TIMEOUT_SECONDS` (mặc định TẮT/5s); seed lỗi `DANGEROUS` -10, không liệt. Idempotent, không sửa V1–V3. |
| Migration V5 | `db/migration/V5__add_intersection_crossings.sql` | 11 bài -> 14 mục: thêm ngã tư (2)/(3)/(4) (LESSON_NO 7/9/13, lời loa chung lesson-05), đổi LESSON_NO 7->8, 8->10, 9->11, 10->12, 11->14 (chỉ preset mẫu id=1); 29 bước; lỗi ngã tư quá 20s -5 / quá 30s liệt cho cả 4 lần qua + nhân bản Vượt đèn đỏ / Không xi nhan. Idempotent, không sửa V1–V4. |

## 6. Lệnh vận hành

| Mục tiêu | Lệnh |
|---|---|
| Biên dịch | `mvn clean compile` (máy dev; sandbox dùng javac trực tiếp, xem report S01) |
| Kiểm thử | `mvn test` |
| Đóng gói | `mvn clean package` |
| Chạy bằng Docker | `docker compose up --build` |
| Health | `curl http://localhost:8080/actuator/health` |

## 7. Quy tắc cập nhật mục lục

Khi thêm hoặc đổi module, agent phải cập nhật hàng tương ứng, `ROADMAP.md`,
file feature và report. Khi thêm migration hoặc nguồn cấu hình, cập nhật
phần database. Không đưa secret hoặc log session dài vào index.

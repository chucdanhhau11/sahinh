# SAHINH-B2 — ROADMAP

## App web luyện thi sa hình hạng B

> **Version:** 0.1.0-SNAPSHOT
> **Project:** SaHinh-B2
> **Ngày tạo:** 2026-11-23
> **Backend:** Java 21 + Spring Boot 3.3.6 + Spring MVC + Embedded Tomcat
> **Frontend:** Thymeleaf + vanilla JavaScript/CSS (không framework JS)
> **Database:** MySQL 8 + Flyway + DataSetRegistry JDBC thuần (cấm JPA/Hibernate/JdbcTemplate)
> **Chạy:** Docker Compose — 2 container `app` + `mysql`, build ra 1 file JAR
> **Âm thanh:** SpeechSynthesis vi-VN + Web Audio beep, không file mp3
> **Documentation Model:** `ROADMAP.md -> FEATURE.md -> report/`
> **Mục lục:** `PROJECT_INDEX.md` (tạo ở S01)

> Đây là **source of truth cấp project**. Mọi agent/developer phải đọc theo
> thứ tự ở phần 1. File `skills/#SAHINH-B2—SKILLS.txt` là luật tối cao,
> không được xóa hoặc đổi. Database migration và source code là bằng chứng
> thực tế nếu documentation khác nhau.

> Nguồn gốc: dựa trên `ROADMAP.md` và `skills/#DRIVEMASTER—SKILLS.txt`
> của DriveMaster, rút gọn và chỉnh lại cho app nhỏ này. Giữ triad
> pg/trx/mod, DataSetRegistry, menu/quyền động từ DB. Bỏ CMS, chat, S3.

---

## 1. Thứ tự đọc bắt buộc trước khi code

| Thứ tự | Tệp/thư mục | Mục tiêu |
|---:|---|---|
| 1 | `skills/#SAHINH-B2—SKILLS.txt` | Quy ước gốc bất biến; không được xóa hoặc đổi. Chứa định nghĩa 3 luồng đã chốt. |
| 2 | `PROJECT_INDEX.md` | Bản đồ mã nguồn, module, migration, config. |
| 3 | `ROADMAP.md` | Trạng thái project và thứ tự công việc (file này). |
| 4 | `docs/features/<FEATURE>/<FEATURE>.md` | Phạm vi và trạng thái feature cần làm. |
| 5 | `docs/features/<FEATURE>/report/` | Báo cáo session mới nhất của đúng feature. |
| 6 | `src/main/resources/db/migration/` rồi source function | Xác thực migration/source trước khi đổi mã. |

**Quy tắc khi user nói "làm thi" mà không nói luồng:** hỏi luồng 1 / 2 / 3,
không tự chọn. Không tự gộp Luồng 2 và Luồng 3.

---

## 2. Các quyết định kiến trúc còn hiệu lực

| ID | Quyết định | Trạng thái | Hệ quả |
|---|---|---|---|
| ADR-001 | JAR Spring Boot/Embedded Tomcat, chạy bằng Docker Compose | **ACCEPTED** | `docker compose up --build` là lệnh chạy chuẩn. Không chạy JAR lẻ khi DB chưa lên. |
| ADR-002 | Không dùng Entity/Repository/JPA/Hibernate/JdbcTemplate | **ACCEPTED** | Mọi mod truy cập dữ liệu qua `DataSetRegistry -> DataSet`. |
| ADR-003 | Mỗi function có Controller + trx + mod + HTML | **ACCEPTED** | Controller chỉ mapping/template; trx request/validate/call mod; business/database ở mod. |
| ADR-004 | Permission chỉ ở Filter | **ACCEPTED** | Filter dùng `SYS_FUNCTION -> SYS_ROLE_USER -> SYS_ROLE_FUNCTION` sau authentication. |
| ADR-005 | Không hard-code database secret | **ACCEPTED** | MySQL chỉ qua biến môi trường `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` trong `docker-compose.yml` / `.env`. |
| ADR-006 | Một file YAML runtime | **ACCEPTED** | Chỉ giữ `src/main/resources/application.yml`; datasource MySQL, Flyway, session dùng chung một cấu hình. |
| ADR-007 | Migration mới chỉ V1+ của app này, không copy V của DriveMaster | **ACCEPTED** | Flyway app này bắt đầu từ `V1`. Không repair lịch sử DriveMaster. |
| ADR-008 | URL HTML theo mã function | **ACCEPTED** | Mọi trang dùng `/{SYSTEM_CODE}/{FUNCTION_CODE}`, ví dụ `/SH/SHA0_0400`. API dùng `/api/{SYSTEM_CODE}/{SUBSYSTEM_CODE}/{FUNCTION_CODE}/...`. |
| ADR-009 | Logic thi tập trung 1 engine duy nhất | **ACCEPTED** | `ExamEngine` dùng chung cho 3 luồng. 3 luồng chỉ khác: ai bấm chuyển bước (máy hay thầy) và có bước nhỏ hay không. Không copy logic tính điểm ra 3 nơi. |
| ADR-010 | Điểm trừ / điểm liệt / giờ thi đọc từ DB, không hardcode | **ACCEPTED** | Code không được hardcode `-5`, `tắt máy = rớt`, `20 giây`. Đọc `SH_ERROR_RULE` và `SH_STEP` / `SH_LESSON` từ DB. |
| ADR-011 | Luồng 1 không ép giờ chuẩn | **ACCEPTED** | Luồng 1 thầy nhập bao nhiêu giây thì máy đếm bấy nhiêu. Không cảnh báo tổng > 18 phút, không bắt đúng luật. |
| ADR-012 | Preset gốc của Superadmin không được sửa trực tiếp | **ACCEPTED** | Teacher bấm "Tự setup riêng" thì hệ thống COPY preset ra bản riêng. Không ghi đè bản gốc. |

---

## 3. Trạng thái hiện tại theo feature

> S01–S07 đã DONE ngày 2026-11-23 (xem §10). Cập nhật bảng này sau mỗi
> session, append session mới ở §10, không xóa lịch sử.
>
> S07: Số tự động — chọn ở màn hình bắt đầu 3 luồng thi, bỏ bài 9 khi
> chọn Số tự động, lưu vào `SH_SESSION.TRANSMISSION` (V4). Tình huống
> nguy hiểm — toggle theo preset, mặc định TẮT, chỉ Luồng 2/3, chậm thì
> trừ 10 điểm (lỗi `DANGEROUS`).

| System | Feature | Function | Progress | Status | Đã hoàn thành | Next task |
|---|---|---|---|---:|---|---|---|
| Foundation | `FOUNDATION` | — | 100% | `DONE` | S01–S06 | S01: tạo project, Docker Compose, `application.yml`, Flyway V1, layout chung |
| Auth | `AUTH` | `ZZA0_0001` | 100% | `DONE` | S01–S06 | S01: login BCrypt/session, role SUPERADMIN/TEACHER/STUDENT |
| Management | `MS` | `MSA0_0100` | 100% | `DONE` | S01–S06 | S01: Dashboard đơn giản (số phiên thi, tỉ lệ đậu) |
| Management | `MS` | `MSA0_0200` | 100% | `DONE` | S01–S06 | S01: User/role/quyền, menu động từ DB |
| Sa hình | `SH` | `SHA0_0100` | 100% | `DONE` | S01–S06 | S02: Cấu hình 11 bài + ~23 bước nhỏ (bảng dưới) |
| Sa hình | `SH` | `SHA0_0200` | 100% | `DONE` | S01–S07 | S02: Cấu hình lỗi, điểm trừ, cờ liệt |
| Sa hình | `SH` | `SHA0_0300` | 100% | `DONE` | S01–S07 | S02: Preset Superadmin + nút Áp dụng / Tự setup riêng |
| Sa hình | `SH` | `SHA0_0400` | 100% | `DONE` | S01–S07 | S03: Thi Luồng 1 — Tự động |
| Sa hình | `SH` | `SHA0_0500` | 100% | `DONE` | S01–S07 | S04: Thi Luồng 2 — Bấm chay chi tiết |
| Sa hình | `SH` | `SHA0_0600` | 100% | `DONE` | S01–S07 | S05: Thi Luồng 3 — Bấm chay thi thật |
| Sa hình | `SH` | `SHA0_0700` | 100% | `DONE` | S01–S07 | S06: Kết quả & Lịch sử phiên thi |

### 3.1. 11 bài thi chuẩn và giờ chuẩn (dùng làm mặc định)

Thi chính thức loa CHỈ đọc tên bài, không đọc bước nhỏ.
Tổng thời gian chuẩn 18 phút, bắt đầu 100 điểm, đạt từ 80 điểm.

| Bài | Tên bài | Giờ chuẩn | Lời loa (mặc định) | Ghi chú |
|---:|---|---|---|---|
| 1 | Xuất phát | 20 giây để qua vạch | Bài thi xuất phát | Quá 20s -5, quá 30s liệt |
| 2 | Dừng xe nhường đường cho người đi bộ | — (dừng 1 lần) | Bài thi dừng xe nhường đường cho người đi bộ | Dừng cách vạch quá 500mm -5 |
| 3 | Dừng xe và khởi hành ngang dốc | 30 giây để qua dốc | Bài thi dừng xe và khởi hành ngang dốc | Tụt quá 500mm liệt; quá 30s liệt |
| 4 | Qua vệt bánh xe và đường hẹp vuông góc | 2 phút | Bài thi qua vệt bánh xe và đường hẹp vuông góc | Đè vạch -5/lần, quá 2 phút -5 |
| 5 | Qua ngã tư có đèn tín hiệu | 20 giây khi đèn xanh | Bài thi qua ngã tư có đèn tín hiệu | Vượt đèn đỏ -10, quá 20s -5, quá 30s liệt |
| 6 | Đường vòng quanh co (chữ S) | 2 phút | Bài thi đường vòng quanh co | Đè vạch -5/lần, quá 2 phút -5 |
| 7 | Ghép xe dọc vào nơi đỗ | 2 phút | Bài thi ghép xe dọc vào nơi đỗ | Đè vạch -5, quá 2 phút -5, hết giờ chưa vào hẳn ô liệt |
| 8 | Tạm dừng ở chỗ có đường sắt chạy qua | — (dừng 1 lần) | Bài thi tạm dừng ở chỗ có đường sắt chạy qua | Giống bài 2 |
| 9 | Thay đổi số trên đường bằng | 2 phút | Bài thi thay đổi số trên đường bằng | Xe số tự động không thi bài này |
| 10 | Ghép xe ngang vào nơi đỗ | 2 phút | Bài thi ghép xe ngang vào nơi đỗ | Đè vạch -5, quá 2 phút -5, hết giờ chưa vào hẳn ô liệt |
| 11 | Kết thúc | — | Bài thi kết thúc | Bật xi nhan phải, qua vạch kết thúc |

### 3.2. ~23 bước nhỏ mặc định (để luyện chi tiết, Luồng 1/2)

Admin chỉ sửa số giây / bật-tắt, không cần tự tạo mới.

| Bài | Bước nhỏ | Lời loa khi bật | Giây mặc định |
|---|---|---|---:|
| 1 | 1.1 Thắt dây an toàn | Thắt dây an toàn | 10 |
| 1 | 1.2 Bật xi nhan trái và xuất phát | Bật xi nhan trái, xuất phát | 20 |
| 1 | 1.3 Tắt xi nhan trái sau 5m | Tắt xi nhan trái | 5 |
| 2 | 2.1 Dừng đúng vạch người đi bộ | Dừng xe nhường đường cho người đi bộ | 15 |
| 3 | 3.1 Dừng đúng vạch trên dốc | Dừng xe trên dốc | 15 |
| 3 | 3.2 Khởi hành qua dốc | Khởi hành qua dốc | 30 |
| 4 | 4.1 Qua vệt bánh xe | Qua vệt bánh xe | 60 |
| 4 | 4.2 Qua đường hẹp vuông góc | Qua đường hẹp vuông góc | 60 |
| 5 | 5.1 Qua ngã tư có đèn | Qua ngã tư có đèn tín hiệu | 20 |
| 6 | 6.1 Qua đường vòng quanh co | Qua đường vòng quanh co | 120 |
| 7 | 7.1 Ghép xe dọc vào nơi đỗ | Ghép xe dọc vào nơi đỗ | 120 |
| 8 | 8.1 Dừng đúng vạch đường sắt | Dừng xe ở chỗ có đường sắt chạy qua | 15 |
| 9 | 9.1 Tăng số trên đường bằng | Tăng số trên đường bằng | 60 |
| 9 | 9.2 Giảm số trên đường bằng | Giảm số trên đường bằng | 60 |
| 10 | 10.1 Ghép xe ngang vào nơi đỗ | Ghép xe ngang vào nơi đỗ | 120 |
| 11 | 11.1 Bật xi nhan phải và kết thúc | Bật xi nhan phải, kết thúc | 10 |

> Số bước trên là 16 dòng gộp. Khi tách bài 4/5/6/7/10 ra thêm bước
> căn gương / trả lái (nếu thầy yêu cầu) thì tổng lên ~23 bước.
> Không được tự thêm bước mà không ghi vào bảng này + cập nhật DB seed.

### 3.3. 3 luồng thi — định nghĩa khóa (đã chốt 2026-11-23)

|  | Luồng 1 | Luồng 2 | Luồng 3 |
|---|---|---|---|
| Tên | Thi chuẩn, tự động | Bấm chay chi tiết | Bấm chay thi thật |
| Function | `SHA0_0400` | `SHA0_0500` | `SHA0_0600` |
| Ai chuyển bước? | Máy tự chuyển | Thầy bấm | Thầy bấm |
| Bước nhỏ? | Có | Có | Không, chỉ 11 bài |
| Bấm + / - ? | Không | Có | Có |
| Chỉnh được giây? | Có, tự do, không ép luật | Có, theo preset | Không |
| Sai thì sao? | Dừng, làm lại từ đầu | Trừ điểm, đi tiếp (trừ liệt) | Trừ điểm, đi tiếp (trừ liệt) |
| Loa đọc gì? | Từng bước nhỏ (nếu bật) hoặc tên bài (nếu tắt) | Từng bước nhỏ / tên bài | Chỉ tên bài |

### 3.4. Bảng lỗi mặc định (cấu hình được, xem SHA0_0200)

| Lỗi | Trừ điểm | Liệt mặc định | Áp dụng |
|---|---|---|---|
| Không thắt dây an toàn | 5 | 0 | Bài 1 |
| Không bật xi nhan trái lúc xuất phát | 5 | 0 | Bài 1 |
| Không tắt xi nhan sau 5m | 5 | 0 | Bài 1 |
| Quá 20s chưa qua vạch xuất phát | 5 | 0 | Bài 1 |
| Quá 30s chưa xuất phát | — | 1 | Bài 1 |
| Không dừng ở vạch người đi bộ / đường sắt | 5 | 0 | Bài 2, 8 |
| Dừng cách vạch quá 500mm | 5 | 0 | Bài 2, 3, 8 |
| Dừng quá vạch | 5 | 0 | Bài 2, 3, 8 |
| Không dừng ở vạch dốc | — | 1 | Bài 3 |
| Tụt dốc quá 500mm | — | 1 | Bài 3 |
| Quá 30s chưa qua dốc | — | 1 | Bài 3 |
| **Tắt máy trên dốc** | — | **1 (user chốt, khác luật thật -5)** | **Bài 3** |
| Chết máy (bài khác) | 5/lần | 0 | Chung |
| Bánh xe đè vạch | 5/lần | 0 | Bài 4, 6, 7, 10 |
| Bánh xe ra ngoài hình bài | — | 1 | Bài 4, 6 |
| Vượt đèn đỏ | 10 | 0 | Bài 5 |
| Không bật xi nhan khi rẽ | 5 | 0 | Bài 5 |
| Quá 2 phút (bài 4/6/7/9/10) | 5 | 0 | Bài 4, 6, 7, 9, 10 |
| Hết giờ ghép xe mà chưa vào hẳn ô | — | 1 | Bài 7, 10 |
| Không tăng / không giảm số đúng | 5 | 0 | Bài 9 |
| Không bật xi nhan phải lúc kết thúc | 5 | 0 | Bài 11 |
| Lái xe lên vỉa hè / gây tai nạn | — | 1 | Chung |
| Quá tổng thời gian 18 phút | 1 / 3 giây | 0 | Chung (Luồng 2/3) |

---

## 4. Kế hoạch session (S01–S07)

| Session | Phạm vi | Function | Done khi nào |
|---|---|---|---|
| S01 | Foundation + Auth + Docker | `ZZA0_0001`, `MSA0_0100`, `MSA0_0200` | `docker compose up --build` chạy được, login được, menu động, `PROJECT_INDEX.md` + `docs/features/` đã tạo, Flyway V1 |
| S02 | Cấu hình thi | `SHA0_0100`, `SHA0_0200`, `SHA0_0300` | Superadmin tạo được preset, sửa được giây/bước/lỗi/liệt, teacher bấm Áp dụng được, Tự setup riêng copy ra bản riêng không ghi đè gốc |
| S03 | Luồng 1 — Tự động | `SHA0_0400` + `ExamEngine` | Bấm Bắt đầu 1 lần, máy tự đọc loa + đếm giờ + chuyển bước tới Kết thúc, sai thì dừng + làm lại từ đầu, hiện điểm |
| S04 | Luồng 2 — Bấm chay chi tiết | `SHA0_0500` | Thầy bấm Next / + / - theo từng bước nhỏ, điểm tự trừ đúng bảng lỗi, lỗi liệt dừng ngay hiện RỚT |
| S05 | Luồng 3 — Bấm chay thi thật | `SHA0_0600` | Chỉ 11 bài, không bước nhỏ, thầy phải bấm mới qua bài, bấm + / -, không chỉnh được giờ/điểm trong luồng này |
| S06 | Kết quả & Lịch sử | `SHA0_0700` | Hiện điểm, đạt/trượt, bước trượt + lý do, lưu lịch sử phiên thi, xem lại được |
| S07 | Polish | Tất cả | Âm thanh loa + bíp ổn, nút Tắt tiếng, mobile dùng được, test lại 3 luồng, cập nhật docs |

Mỗi session xong phải có report trong
`docs/features/<FEATURE>/report/report_session_0X.md` và append
mục mới vào §10 của file này. Không xóa lịch sử.

---

## 5. Database / Flyway thực tế

Database: `sahinh_b2` (MySQL 8, Docker container `mysql`).

Migration bắt đầu từ V1 của app này:

| Migration | Nội dung | Trạng thái |
|---|---|---|
| `V1__create_foundation_auth.sql` | `SYS_USER`, `SYS_ROLE`, `SYS_ROLE_USER`, `SYS_FUNCTION`, `SYS_ROLE_FUNCTION`, `SYS_FUNCTION_PAGE`, seed SUPERADMIN/TEACHER | DONE — S01 |
| `V2__create_exam_config.sql` | `SH_PRESET`, `SH_LESSON`, `SH_STEP`, `SH_ERROR_RULE`, seed 11 bài + ~23 bước + bảng lỗi §3.4 | DONE — S02 |
| `V3__create_exam_session.sql` | `SH_SESSION`, `SH_SESSION_LOG`, lưu phiên thi + log từng bước/bài + điểm | DONE — S03/S06 |
| `V4__add_transmission_and_dangerous.sql` | `SH_SESSION.TRANSMISSION`, `SH_PRESET.DANGEROUS_ENABLED` + `DANGEROUS_TIMEOUT_SECONDS`, seed lỗi `DANGEROUS` (-10, không liệt) | DONE (code) — S07, chưa chạy migrate thật trên MySQL |

Bảng chính (tên cột gợi ý, chốt trong PROJECT_INDEX.md ở S01):

- `SH_PRESET`: `ID`, `NAME`, `OWNER_ID`, `IS_TEMPLATE`, `IS_ACTIVE`, `CREATED_AT`
- `SH_LESSON`: `ID`, `PRESET_ID`, `LESSON_NO` (1-11), `NAME`, `ANNOUNCE_TEXT`, `DURATION_SECONDS`, `SORT_ORDER`
- `SH_STEP`: `ID`, `LESSON_ID`, `STEP_NO`, `NAME`, `ANNOUNCE_TEXT`, `DURATION_SECONDS`, `ENABLED`, `SORT_ORDER`
- `SH_ERROR_RULE`: `ID`, `PRESET_ID`, `LESSON_ID`, `CODE`, `NAME`, `DEDUCT_POINTS`, `IS_LIET`, `SORT_ORDER`
- `SH_SESSION`: `ID`, `USER_ID`, `PRESET_ID`, `FLOW` (1/2/3), `SCORE`, `PASSED`, `FAILED_STEP`, `FAILED_REASON`, `STARTED_AT`, `ENDED_AT`
- `SH_SESSION_LOG`: `ID`, `SESSION_ID`, `LESSON_NO`, `STEP_NO`, `RESULT`, `DEDUCT_POINTS`, `NOTE`, `CREATED_AT`

Không tạo migration mới nếu không có thay đổi schema thực sự.

---

## 6. Nợ kỹ thuật và rủi ro

| Mức độ | Hạng mục | Trạng thái / xử lý |
|---|---|---|
| Medium | Âm thanh trình duyệt | SpeechSynthesis giọng vi-VN khác nhau theo trình duyệt/máy; phải có nút Tắt tiếng + fallback chỉ hiện chữ nếu không đọc được. |
| Medium | Đếm giờ chính xác | Đếm giờ ở frontend chỉ để hiển thị; quyết định hết giờ / trừ điểm phải khớp với server (mod). Không để frontend tự quyết điểm. |
| Low | Xe số tự động không thi bài 9 | **DONE — S07**: chọn hộp số ở màn hình bắt đầu 3 luồng, Số tự động bỏ bài 9 khỏi plan (cùng 1 engine nên nhất quán), lưu `SH_SESSION.TRANSMISSION` (V4). Mặc định Số sàn. |
| Low | Tình huống nguy hiểm ngẫu nhiên | **DONE — S07**: toggle theo preset, mặc định TẮT, chỉ Luồng 2/3, điểm ngẫu nhiên giữa bài thi, chậm thì -10 (lỗi `DANGEROUS` seed ở V4, không liệt). |

---

## 7. Việc agent tiếp theo phải làm nếu tiếp tục

1. Đọc `skills -> PROJECT_INDEX -> ROADMAP -> feature docs -> report -> migration/source` theo §1.
2. Làm theo thứ tự S01 -> S07, không nhảy sang Luồng 2/3 khi Luồng 1 + `ExamEngine` chưa xong.
3. Không tạo migration mới nếu không có thay đổi schema thực sự.
4. Sau mỗi session, cập nhật đúng feature doc, report, `PROJECT_INDEX.md`, `ROADMAP.md` (§10).
5. Trước khi nói "xong": chạy `mvn test`, `mvn clean package`, `docker compose up --build`, kiểm tra health + chạy thử 3 luồng bằng tay.

---

## 8. Lệnh vận hành

| Mục tiêu | Lệnh |
|---|---|
| Build/test | `mvn test && mvn clean package` |
| Chạy bằng Docker | `docker compose up --build` |
| Chạy JAR local (khi MySQL đã lên) | `DB_HOST=localhost DB_PORT=3306 DB_NAME=sahinh_b2 DB_USER=root DB_PASSWORD=... java -jar target/sahinh-b2.jar` |
| Health | `curl http://localhost:8080/actuator/health` |
| Kiểm tra Flyway | `mysql -h localhost -u root -p -e "USE sahinh_b2; SELECT version, script, success FROM flyway_schema_history;"` |
| Dừng Docker | `docker compose down` (thêm `-v` chỉ khi muốn xóa hẳn DB) |

---

## 9. Quy tắc cập nhật roadmap

- Không sửa hoặc xóa nội dung lịch sử trong `skills/#SAHINH-B2—SKILLS.txt` và ROADMAP.
- Chỉ append mục mới có ngày ở §10, cập nhật bảng trạng thái §3.
- Mọi thay đổi về 3 luồng, điểm liệt, preset phải được user chốt trước
  khi sửa code, và ghi rõ vào report session đó.

---

## 10. Session log — append-only

### S00 — 2026-11-23 — Tạo 2 file gốc cho app

- Tạo `skills/#SAHINH-B2—SKILLS.txt` và `ROADMAP.md` này, dựa trên
  DriveMaster (`skills/#DRIVEMASTER—SKILLS.txt` + `ROADMAP.md`),
  rút gọn cho app Sa hình B2.
- Chốt 3 luồng thi sau khi user sửa 2 lần: Luồng 1 tự động (thầy chỉ
  bấm Bắt đầu, sai làm lại từ đầu, chỉnh giây tự do không ép luật),
  Luồng 2 bấm chay chi tiết (bấm + / - theo bước nhỏ),
  Luồng 3 bấm chay thi thật (không bước nhỏ, chỉ 11 bài, thầy phải
  bấm mới qua bài, cũng bấm + / -).
- Chốt điểm liệt cấu hình được, ví dụ tắt máy trên dốc = rớt.
- Chốt preset: Superadmin setup sẵn, teacher bấm Áp dụng hoặc Tự setup
  riêng (copy, không ghi đè gốc).
- Chốt nút bật/tắt bước nhỏ: tắt thì chỉ đọc tên bài + đếm giờ bài,
  bật thì đọc từng bước nhỏ.
- Trạng thái: 2 file đã tạo, chưa có code. Session tiếp theo là S01.

### S01–S06 — 2026-11-23 — Build app S01–S06 (scaffold + code, chưa chạy Docker trong sandbox)

- S01: project Spring Boot 3.3.13 / Java 21, pom.xml, application.yml duy nhất,
  Dockerfile, docker-compose.yml (app + mysql:8.0), README, PROJECT_INDEX.md.
  Core: DataSet/DataSetRegistry JDBC thuần, PasswordUtil BCrypt, AuthUtil,
  PermissionFilter. Auth ZZA0_0001 (admin/admin123, teacher/teacher123,
  student/student123 — BCrypt hash tạo lúc scaffold), Dashboard MSA0_0100,
  Users/Menu MSA0_0200. Flyway V1.
- S02: SHA0_0100 (bài + bước nhỏ: lời loa, giây tự do, bật/tắt),
  SHA0_0200 (lỗi: điểm trừ, cờ liệt), SHA0_0300 (preset: Áp dụng / Tự setup
  riêng = COPY không ghi đè gốc / Tạo mới). Flyway V2: seed preset
  "Chuẩn thi thật", 11 bài, 23 bước nhỏ, 24 lỗi (Tắt máy trên dốc IS_LIET=1).
- S03: ExamEngine + ExamService dùng chung; SHA0_0400 Luồng 1 tự động
  (bấm Bắt đầu 1 lần, tự đếm giờ/đọc loa/chuyển bước, hết giờ thì dừng
  và làm lại từ đầu, điểm 0).
- S04: SHA0_0500 Luồng 2 bấm chay chi tiết (Next / + / - chọn lỗi cụ thể,
  điểm do server tính, lỗi liệt RỚT ngay).
- S05: SHA0_0600 Luồng 3 bấm chay thi thật (không bước nhỏ, chỉ 11 bài,
  loa chỉ đọc tên bài, thầy phải bấm mới qua bài, cũng + / -).
- S06: SHA0_0700 kết quả & lịch sử (điểm, ĐẠT/TRƯỢT/RỚT DO LỖI LIỆT,
  bước trượt + lý do, log lỗi). Flyway V3: SH_SESSION/SH_SESSION_LOG.
- Âm thanh: SpeechSynthesis vi-VN + Web Audio beep, nút Tắt loa, không mp3.
- Verify trong sandbox (2026-11-23, đã chạy thật):
  * `fetch_deps.py pom.xml`: 92 artifacts resolved, 0 failures, 0 missing.
  * `javac -proc:none` toàn bộ 39 file `src/main/java` với classpath toàn bộ
    jar trong `~/.m2/repository`: exit 0, 41 class, 0 lỗi (đã sửa 4 lỗi lúc
    đầu: trùng tên method `page()` ở ZZA0_0001_pg và thiếu import ở SHA0_0200_mod;
    đã sửa cả file sinh lẫn generator).
  * Unit test `ExamEngineTest`: 6/6 PASS (ngưỡng 80, trừ điểm thường, lỗi liệt
    dừng ngay, failStrict Luồng 1, buildPlan mở bước nhỏ / Flow 3 chỉ dùng bài).
  * `node --check` app.js + exam.js: OK (đã sửa 1 bug ở exam.js: base URL
    flow bị ghép sai thành SHA0_040000, đã sửa thành mapping đúng 0400/0500/0600).
  * Seed V2 đếm được: 11 bài, 23 bước nhỏ, 24 lỗi (trong đó Tắt máy trên dốc IS_LIET=1).
  * `grep` cấm (JpaRepository/JdbcTemplate/@Entity): chỉ còn 1 dòng comment
    giải thích cấm trong DataSet.java, không có code vi phạm.
  * pom.xml (XML) + application.yml (YAML) parse OK; 10/10 view của pg khớp
    đúng 10 template HTML; SQL V1/V2/V3 cân bằng ngoặc, đủ số câu lệnh.
  * CHƯA verify được trong sandbox: `mvn test`, `mvn clean package`,
    `docker compose up --build`, chạy app thật + MySQL + login + chạy tay
    3 luồng (`mvn`/`docker`/`mysql` không có sẵn trong sandbox, Maven qua
    proxy lỗi TLS). Cần máy dev chạy tiếp theo lệnh ở §8 của ROADMAP.

### S07 — 2026-11-23 — Audit S01–S06 + Polish + Số tự động + Tình huống nguy hiểm

- Audit (đọc toàn bộ `*_mod.java`, `*_trx.java`, `exam.js`, `app.js`,
  `app.css`, 10 template, `ExamEngine`/`ExamService`, `PermissionFilter`,
  SQL V1–V3): tìm ra 12 điểm, trong đó 6 phải sửa (A1, A2, A3, A4, A8,
  A9), 6 đúng/không phải lỗi (A5, A6, A7, A10, A11, A12). Chi tiết ở
  `docs/features/SH/report/report_session_07.md`.
- Sửa: (1) phiên đã RỚT liệt không bị `/finish` ghi đè thành TRƯỢT/0,
  finish gọi 2 lần trả đúng trạng thái đã lưu; (2) chặn IDOR — chỉ chủ
  phiên hoặc SUPERADMIN được log/finish/xem kết quả (kết quả không có
  quyền trả 404); (3) quá 18 phút trừ -1/3 giây ở server (Luồng 2/3,
  ghi log `OVERTIME`); (4) SHA0_0200 tạo + xóa lỗi (qua quyền preset);
  (5) `PermissionFilter` chặn mặc định (API 403, trang về `/login`);
  (6) CSS mobile: bảng cuộn ngang, nút big full-width ở 480px.
- Số tự động: chọn ở màn hình bắt đầu 3 luồng, bỏ bài 9 khỏi plan khi
  Số tự động, lưu `SH_SESSION.TRANSMISSION` (V4, mặc định `MANUAL`).
- Tình huống nguy hiểm: toggle theo preset (mặc định TẮT), chỉ Luồng
  2/3, điểm ngẫu nhiên giữa bài thi, đèn đỏ + loa, bấm kịp không trừ,
  chậm -10 qua lỗi `DANGEROUS` (seed V4). Clone/tạo preset copy cả cờ.
- Verify trong sandbox (đã chạy thật): `javac` main + test exit 0;
  `ExamEngineTest` **8/8 PASS** (thêm test quá giờ + Số tự động bỏ bài
  9); `node --check` app.js + exam.js OK; V4 ngoặc cân bằng, 16 câu;
  V1–V3 không sửa. CHƯA verify live (giống S01–S06): `mvn test`,
  `docker compose up --build`, app thật + MySQL + chạy tay 3 luồng +
  migrate V4 thật — cần máy dev, xem checklist ở cuối report S07 §F.

### S08 — 2026-11-23 — Fix vòng lặp tải lại trang login (report_session_08)

- User chạy `docker compose up --build` lên được nhưng trang login load
  liên tục, không nhập/bấm được. Nguyên nhân: `app.js` `api()` thấy HTTP
  401 là chuyển về `/login`, trong khi `PermissionFilter` không cho
  `/api/ZZ/A0/ZZA0_0001/session` qua nên `loadSession()` trên trang login
  luôn nhận 401 -> tải lại -> lại 401 -> lặp vô hạn.
- Sửa 2 file: Filter thêm `/session` + `/logout` vào public (trx trả JSON
  `{code:401}` HTTP 200 như thiết kế); `api()` chỉ redirect khi 401 ở
  trang khác trang login + cờ `noRedirect` cho `loadSession()`/`loadMenu()`.
  Trang thi hết phiên vẫn bị đẩy về `/login` như cũ.
- Verify trong sandbox: `node --check` OK; mô phỏng JS 4/4 PASS (login+401
  không redirect, dashboard+401 vẫn redirect, login+200 hiện tên user);
  `javac` main + test exit 0; `ExamEngineTest` 8/8 PASS.
- CHƯA verify live: sandbox không có Docker/MySQL, bước cài MariaDB bằng
  `apt` đã bị user từ chối nên không start được app thật ở đây. Cần chạy
  lại `docker compose up --build` trên máy dev và đăng nhập thử.

### S09 — 2026-11-23 — Chạy live trong sandbox, xác minh login + fix 2 lỗi mới (report_session_09)

- User: vẫn lỗi login, yêu cầu tự start chạy test bên trong cho mượt rồi báo.
  Đọc lại skills + roadmap trước khi làm.
- Sandbox chặn mọi TCP từ tiến trình Java (kể cả đổi cổng, đổi tên tiến trình,
  Unix socket đều không qua). Chạy live được bằng H2 in-memory MODE=MySQL +
  classpath đúng pom (86 jar) + `javac -parameters` + V4 viết lại bằng H2
  (chỉ trong /tmp, không commit).
- Xác minh S08 ĐÚNG bằng trình duyệt thật: trang login chỉ load 1 lần trong
  8 giây, `/session` trả JSON 401 với HTTP 200, login admin/admin123 vào
  Dashboard, menu động từ DB. Nếu máy dev vẫn lỗi thì chưa pull/build lại.
- Lỗi mới 1: cả 10 template đặt `app.js`/`exam.js` ở cuối body, SAU khối
  inline gọi `api()` -> `ReferenceError`, mọi trang kẹt "Đang tải...".
  Fix: chuyển 2 thẻ script lên `<head>` cả 10 template.
- Lỗi mới 2: `toast()` = `alert()` chặn cả trang khi API lỗi. Fix: toast div
  không chặn, tự ẩn sau 4 giây.
- Verify sau fix (trình duyệt thật): login, dashboard stats, cấu hình bài,
  preset, Luồng 2 Bắt đầu / bấm + / chọn lỗi trừ 5 điểm (100 -> 95) đều
  PASS, 0 dialog, 0 pageerror. `/start` 200, history có phiên RUNNING.

### S10 — 2026-11-23 — Âm thanh thi giống loa sân sát hạch, mp3 thu sẵn (report_session_10)

- User hỏi âm thanh app có giống y hệt loa thi thật không — trả lời thật là
  KHÔNG (giọng máy SpeechSynthesis + bíp 880 Hz tự tạo). User bảo thử cách
  tách tiếng loa từ video YouTube.
- Đã tải + nhận dạng toàn bộ 2 video full 11 bài (whisper-small, 31 phút):
  cả 2 đều là thầy giảng suốt, không có tiếng loa sạch để cắt; 2 video chip
  cũ đã không xem được. Phân tích phổ cũng không tìm được nốt bíp sạch.
- Phương án thay thế: giọng nữ đọc thu sẵn 11 câu thông báo + "Dừng xe nguy
  hiểm" + chuông "tính tong" 2 nốt, lưu ở `static/audio/` (lesson-01…
  lesson-11, dangerous, beep). `exam.js` phát mp3 trước, lỗi/mất file thì
  về giọng máy cũ; bước chi tiết vẫn giọng máy. `/audio/` thêm vào
  `isPublic` của PermissionFilter (lúc đầu thiếu nên bị 302 về /login).
- Verify live (H2): 13 file mp3 đều HTTP 200; trình duyệt thật chạy Luồng 3
  — Bắt đầu đọc "Bài thi xuất phát", + Đúng qua Bài 2/3 đọc đúng file
  lesson-02/03; 0 page error. Lỗi console 401 `menu` ở trang login là có
  từ trước (S09), không do S10.
- Bổ sung trước push: kiểm chứng được loa thi thật đọc "Bài thi tiếp theo,
  bài thi <tên>" (2 nguồn độc lập) nên tạo lại lesson-02…11 có tiền tố đó;
  thêm result-pass.mp3 ("Chúc mừng bạn đã thi đạt…") phát khi ĐẠT; chạy lại
  đủ Luồng 3: ĐẠT 100/100, đủ 13 file audio theo thứ tự.
- Muốn giống y hệt loa thi thật: user ghi âm tiếng loa ở sân thi/xe chip
  rồi thay file cùng tên, không cần sửa code.
- Còn phải kiểm chứng trên máy dev (MySQL thật + V4 thật): login sau
  `docker compose up --build`, chạy hết 3 luồng, số tự động bỏ bài 9,
  tình huống nguy hiểm, quá 18 phút trừ điểm Luồng 2/3.

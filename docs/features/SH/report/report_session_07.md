# S07 — Audit S01–S06 + Polish + Số tự động + Tình huống nguy hiểm

**Ngày:** 2026-11-23
**Phạm vi:** Audit toàn bộ code S01–S06 (đọc Skills -> PROJECT_INDEX ->
ROADMAP -> PROMPT -> report 02–06 -> toàn bộ `*_mod.java`, `*_trx.java`,
`ExamEngine`/`ExamService`, `exam.js`, `app.js`, `app.css`, 10 template,
`PermissionFilter`, SQL V1–V3), sửa các khoảng trống thật, làm S07 polish,
thêm chọn hộp số, thêm toggle tình huống nguy hiểm (mặc định TẮT).

## A. Audit — danh sách khoảng trống (ghi trước khi sửa)

### A1. BUG — Trạng thái RỚT bị mất khi kết thúc phiên (ExamService.finishSession)
`finishSession()` kiểm tra `if (failed)` TRƯỚC, nên khi `exam.js`
`applyError()` đã ghi lỗi liệt (session đã `STATUS='ROT'`) rồi gọi
`/finish` với `failed=true`, server ghi đè thành `score=0`,
`STATUS='FAILED'`. Kết quả hiển thị thành "TRƯỢT" thay vì
"RỚT DO LỖI LIỆT", điểm thật cũng mất. Mức: **nghiêm trọng, phải sửa**.

### A2. BUG — Không kiểm tra quyền sở hữu phiên thi (IDOR)
`ExamService.resultOf(sessionId)` không kiểm tra user/role; các endpoint
`/log`, `/finish`, `/result` của 3 luồng nhận `sessionId` bất kỳ mà không
truyền user đang đăng nhập. Học viên/thầy đã đăng nhập có thể xem, ghi
điểm, kết thúc phiên thi của người khác chỉ bằng cách đổi `sessionId`.
`SHA0_0700` list history thì đã lọc đúng (SUPERADMIN tất cả, còn lại chỉ
của mình) — chỉ thiếu ở 3 endpoint trên. Mức: **nghiêm trọng, phải sửa**.

### A3. THIẾU — Quá tổng thời gian 18 phút (-1 điểm / 3 giây) không được
### thi hành ở server trong Luồng 2/3
Không có chỗ nào tính thời gian đã trôi qua của phiên thi. Đồng hồ ở
`exam.js` chỉ hiển thị; `finishSession()` chỉ lấy điểm hiện tại rồi
kết luận ĐẠT/TRƯỢT. Luật "quá 18 phút cứ 3 giây -1 điểm" (PROMPT §3,
ROADMAP §3.4) chưa hề chạy. Luồng 1 được miễn (giây chỉnh tự do, không
ép luật — theo Skills). Mức: **phải sửa (chỉ Luồng 2/3)**.

### A4. THIẾU — SHA0_0200 chỉ sửa lỗi, không tạo/xóa lỗi
`SHA0_0200_mod` (44 dòng) chỉ có `listErrors` + `updateError`
(điểm trừ, cờ liệt). Không có tạo lỗi mới / xóa lỗi. Mức: **sửa** —
thêm tạo + xóa (kèm kiểm tra quyền sở hữu preset như `updateError`).

### A5. KHÔNG PHẢI LỖI — SHA0_0100 chỉ sửa bài/bước, không tạo bài/bước
`SHA0_0100_mod` chỉ có `updateLesson` / `updateStep` / `setDetailed`.
Đây là **đúng thiết kế**: 11 bài là cố định theo luật thi, seed đã có
đủ 23 bước; admin chỉ sửa lời loa / giây / bật-tắt. Không thêm tạo/xóa
bài/bước (tạo thêm bài sẽ phá thứ tự 11 bài chuẩn). Ghi nhận, không sửa.

### A6. ĐÚNG — Tên học viên được lưu
`ExamService.startSession()` INSERT `STUDENT_NAME` vào `SH_SESSION`
(cột có trong V3), trang Kết quả hiển thị cột Học viên. Không phải lỗi.

### A7. ĐÚNG — Luồng 1 hết giờ có gọi server
`exam.js` khi hết giờ ở Luồng 1 gọi `finish(true, ...)` -> POST `/finish`
-> `finishSession(failed=true)` đặt điểm 0, `STATUS='FAILED'`. Đúng.
Lưu ý nhỏ (không sửa): đóng tab giữa chừng thì phiên ở lại `RUNNING`.

### A8. LỖI NHỎ — PermissionFilter fail-open với đường dẫn lạ
Khi không tìm được `FUNCTION_ID` cho 1 đường dẫn (`functionId == null`),
filter cho qua luôn thay vì chặn. Đường dẫn không có trong
`SYS_FUNCTION_PAGE` và không phải API sẽ lọt. Mức: **sửa** — chặn mặc
định (API -> 403, trang -> về `/login`), trừ các đường public đã liệt kê.

### A9. THIẾU — CSS mobile mỏng
Chỉ có 1 media query `max-width: 640px` (timer 64px, nút big nhỏ lại).
Bảng ở trang Cấu hình lỗi / Lịch sử không có cuộn ngang, sẽ tràn màn
hình điện thoại; nút + / - chưa đủ lớn khi cầm 1 tay. Mức: **sửa ở S07**.

### A10. GHI NHẬN — Nút "Qua bài" không ghi log
`exam.js` `nextItem()` chỉ chuyển bước, không ghi `SH_SESSION_LOG`.
Đây là "bỏ qua" chứ không phải "đúng", nên không ghi PASS là đúng
ngữ nghĩa. Giữ nguyên, không sửa.

### A11. ĐÚNG — Preset clone / tạo mới có kiểm tra quyền
`SHA0_0300_trx` `/create` kiểm tra `isSuperAdmin`; `/clone` chỉ clone
preset mà user thấy được (template hoặc của mình); `canEditPreset`
chặn sửa trực tiếp preset mẫu. Đúng, không sửa.

### A12. ĐÚNG — Flow 1 / Flow 3 phân biệt rõ
`ExamEngine.buildPlan(useSteps=false)` cho Luồng 3 luôn dùng bài cha;
Luồng 1/2 mở bước nhỏ khi `DETAILED_ENABLED=1`. Đúng, không sửa.

## B. Các sửa đã làm (sau audit)

### B1. A1 — RỚT không bị ghi đè khi kết thúc phiên
`ExamService.finishSession()`: phiên đã `STATUS='ROT'` (do lỗi liệt)
thì giữ nguyên ROT + điểm thật; `failed=true` không còn ghi đè thành
`FAILED`/0. Thêm `finishResult()` — gọi `/finish` lần 2 trả đúng trạng
thái đã lưu (idempotent), không đổi gì thêm.

### B2. A2 — Kiểm tra quyền sở hữu phiên thi (IDOR)
Thêm `requireSessionAccess(session, userId, role)`: chủ phiên hoặc
SUPERADMIN mới được `logEvent` / `finishSession` / `resultOf`.
`SHA0_0400/0500/0600` mod + trx và `SHA0_0700` mod + trx truyền
`currentUserId` + `currentRole` từ `HttpSession` xuống service.
`resultOf` khi không có quyền trả `null` (trx -> 404, không lộ phiên
có tồn tại hay không). `logEvent`/`finishSession` chặn luôn phiên đã
kết thúc (`STATUS != RUNNING` -> lỗi, không ghi log thêm).
Các overload cũ (không user/role) vẫn còn, chỉ dùng nội bộ với quyền
SUPERADMIN mặc định; không còn endpoint nào gọi chúng.

### B3. A3 — Quá 18 phút trừ điểm ở server (Luồng 2/3)
`ExamEngine.overtimeDeduct(elapsedSeconds)`: 0 khi <= 1080 giây, còn
lại `(elapsed - 1080) / 3` (làm tròn xuống). `finishSession()` tính
`TIMESTAMPDIFF(SECOND, STARTED_AT, NOW())`, trừ vào điểm, ghi 1 dòng
`OVERTIME` vào `SH_SESSION_LOG` khi trừ > 0. Luồng 1 không áp dụng
(giây chỉnh tự do, theo Skills).

### B4. A4 — SHA0_0200 tạo + xóa lỗi
`SHA0_0200_mod.createError()` / `deleteError()` + endpoint
`/error/create`, `/error/delete` (trx). Cả hai đều qua
`canEditPreset` (preset mẫu Superadmin không sửa trực tiếp).
Template `SHA0_0200.html`: form "Thêm lỗi mới" (tên, bài, điểm trừ,
cờ liệt) + nút Xóa từng dòng.

### B5. A8 — PermissionFilter chặn mặc định
`functionId == null` giờ gọi `forbid()`: API -> 403 JSON, trang ->
`/login`. Danh sách public không đổi (`/`, `/login`, `/error`, `/css/`,
`/js/`, `/images/`, `/favicon.ico`, `/actuator/health`,
`/api/ZZ/A0/ZZA0_0001/login`).

### B6. A9 — CSS mobile (xem §C)

## C. S07 Polish — âm thanh + mobile

- Âm thanh: `exam.js` `speak()` dùng `SpeechSynthesis` `lang='vi-VN'`
  (rate 0.9, pitch 1), `beep()` Web Audio 880 Hz / 0.15 s, nút Tắt loa
  (`muted`) có trên cả 3 trang thi. `node --check` OK cả 2 file JS.
- Mobile: `.card { overflow-x: auto }`; thêm media query
  `max-width: 480px` (timer 52px, nút big full-width, chữ 18px);
  `max-width: 640px` bổ sung padding container/topbar, score 32px,
  bảng cuộn ngang (`display:block; overflow-x:auto; white-space:nowrap`),
  nút `.row` co 40% / 100%. Không còn tràn ngang ở bảng Cấu hình lỗi /
  Lịch sử.
- Re-test 3 luồng: bằng unit test + code walk (xem §F). Logic luồng
  không đổi: Luồng 1 tự động + làm lại từ đầu khi sai, Luồng 2/3 + / -
  theo preset, liệt dừng ngay.

## D. Số tự động (bỏ bài 9)

- Chọn hộp số ở màn hình bắt đầu của cả 3 luồng thi (select
  `Số sàn` / `Số tự động (bỏ bài 9)`), mặc định Số sàn.
- `ExamEngine.buildPlan(..., transmission)`: `transmission='AUTO'` thì
  bỏ bài 9 khỏi plan (cả 3 luồng, vì cùng 1 engine + 1 plan; tiến độ
  và điểm đều đi qua plan nên nhất quán). `MANUAL`/mặc định giữ đủ 11 bài.
- `ExamService.planFor` / `startSession` có overload truyền
  `transmission`; `plan` trả thêm trường `transmission`.
  `SHA0_0400/0500/0600` mod/trx truyền `transmission` từ request
  (plan: query param, mặc định `MANUAL`; start: body).
- Lưu vào `SH_SESSION.TRANSMISSION` (V4, `VARCHAR(10) DEFAULT 'MANUAL'`).

## E. Tình huống nguy hiểm (toggle, mặc định TẮT) — ĐÃ LÀM

- V4: `SH_PRESET.DANGEROUS_ENABLED TINYINT DEFAULT 0`,
  `SH_PRESET.DANGEROUS_TIMEOUT_SECONDS INT DEFAULT 5`; seed lỗi
  `DANGEROUS` ("Không xử lý tình huống nguy hiểm", trừ 10, không liệt)
  cho mọi preset chưa có.
- `SHA0_0300`: endpoint `/dangerous` + `setDangerous()` (qua
  `canEditPreset`); `clonePreset`/`createPreset` copy cờ nguy hiểm qua
  `copyPresetContent` (bọc try/catch để DB chưa migrate V4 vẫn chạy).
  Template preset: mỗi dòng có checkbox + số giây + nút Lưu.
- `ExamService.planFor` trả thêm `dangerousEnabled`,
  `dangerousTimeoutSeconds`, `dangerousErrorRuleId` (đọc null-safe;
  DB chưa có cột V4 thì coi như TẮT).
- Chỉ Luồng 2/3: khi preset bật, `exam.js` chọn ngẫu nhiên 1 điểm
  giữa bài thi (`hazardIdx`, từ vị trí 2 tới `items.length - 3`);
  tới đúng điểm đó thì dừng đồng hồ, hiện overlay đèn đỏ nhấp nháy +
  đọc loa "Tình huống nguy hiểm", đếm ngược theo số giây cấu hình.
  Bấm kịp "Bật đèn báo nguy hiểm" -> không trừ điểm, chạy lại đồng hồ
  bài/bước đó. Hết giờ không bấm -> `applyError(DANGEROUS)` trừ 10
  điểm qua server (như lỗi thường, không liệt), thi tiếp. Luồng 1
  không có tình huống nguy hiểm. Mặc định preset TẮT thì không đổi gì.

## F. Kiểm thử (sandbox — đã chạy thật 2026-11-23)

- `javac -proc:none` toàn bộ `src/main/java` (classpath toàn bộ jar
  `~/.m2/repository`): **exit 0, 0 lỗi**.
- `javac` toàn bộ `src/test/java`: **exit 0**.
- `ExamEngineTest` (junit-platform-console-standalone 1.10.3):
  **8/8 PASS** (6 cũ + 2 mới: `overtimeDeduct` 6 trường hợp
  1080->0, 1079->0, 1083->1, 1086->2, 1200->40, 0->0; `buildPlan`
  AUTO bỏ bài 9, MANUAL giữ đủ, mặc định giữ đủ).
- `node --check` `app.js` + `exam.js`: **OK**.
- `grep` cấm (JpaRepository/JdbcTemplate/@Entity): chỉ 1 dòng comment
  giải thích trong `DataSet.java` (đã biết từ S01–S06), không vi phạm.
- `pom.xml` parse XML OK; `application.yml` parse YAML OK.
- SQL V4 kiểm tra tĩnh: ngoặc cân bằng, 16 câu lệnh; V1–V3 không sửa.
- Kiểm tra nối dây: `SHA0_0200.html` có biến `presetId` toàn cục;
  `listPresets` dùng `SELECT p.*` nên có cột `DANGEROUS_*` sau V4;
  `exam.js` gửi `transmission` ở cả `/plan` và `/start`.
- **CHƯA verify trong sandbox** (giống S01–S06): `mvn test`,
  `mvn clean package`, `docker compose up --build`, chạy app thật +
  MySQL + login + chạy tay 3 luồng + kiểm tra Flyway V4 áp dụng thật
  (`mvn`/`docker`/`mysql` không có sẵn, Maven qua proxy lỗi TLS).
  Cần máy dev chạy theo ROADMAP §8, đặc biệt kiểm tra: V4 migrate lên
  DB đã có V1–V3; Luồng 2/3 quá 18 phút trừ điểm thật; RỚT liệt giữ
  nguyên khi bấm kết thúc; học viên khác không xem được phiên của nhau
  (403/404); Số tự động bỏ bài 9 ở cả 3 luồng; toggle nguy hiểm bật/tắt.

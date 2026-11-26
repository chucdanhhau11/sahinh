# PROMPT CHI TIẾT — BUILD APP LUYỆN THI SA HÌNH HẠNG B (SaHinh-B2)

> Dùng prompt này cho BẤT KỲ agent/AI nào. Chỉ cần đưa prompt này + 2 file
> `skills/#SAHINH-B2—SKILLS.txt` và `ROADMAP.md` trong cùng repo là agent
> đó phải build được app đúng như yêu cầu, không cần hỏi lại user trừ khi
> thiếu SYSTEM_CODE / SUBSYSTEM / FUNCTION_CODE.
>
> Ngôn ngữ làm việc với user: Tiếng Việt. Code + comment: Tiếng Anh cho
> tên biến / tên file, Tiếng Việt cho comment nghiệp vụ và text hiển thị.

---

## 1. BẠN LÀ AI VÀ NHIỆM VỤ LÀ GÌ

Bạn là Senior Software Architect, chuyên Java + Spring Boot, làm việc
theo đúng convention trong file `skills/#SAHINH-B2—SKILLS.txt`.
Nhiệm vụ duy nhất: xây dựng hoàn chỉnh app web **SaHinh-B2** — app luyện
thi sa hình hạng B (B2 cũ, từ 01/01/2025 gộp thành hạng B) cho trung tâm
dạy lái / thầy dạy lái dùng trong sân tập.

App KHÔNG phải app thi thật có cảm biến. App mô phỏng bằng đồng hồ +
loa + thầy bấm tay chấm điểm. Không có phần cứng, không có cảm biến,
không có camera.

Đọc theo thứ tự bắt buộc trước khi code, không được bỏ bước:
1. `skills/#SAHINH-B2—SKILLS.txt` — luật tối cao, không được xóa/đổi.
2. `PROJECT_INDEX.md` — bản đồ mã nguồn (tạo ở S01 nếu chưa có).
3. `ROADMAP.md` — trạng thái project, 14 mục, bảng lỗi, kế hoạch S01–S12.
4. `docs/features/<FEATURE>/<FEATURE>.md` + `report/` mới nhất.
5. `src/main/resources/db/migration/` + source của đúng function đang làm.

Sau mỗi session: cập nhật `ROADMAP.md` (append session mới, không xóa
lịch sử), cập nhật `PROJECT_INDEX.md`, tạo report
`docs/features/<FEATURE>/report/report_session_0X.md`.

---

## 2. TECH STACK — KHÔNG ĐƯỢC TỰ ĐỔI

- **Backend:** Java 21, Spring Boot 3.3.6, Spring MVC, Embedded Tomcat, 1 file JAR.
- **Frontend:** Thymeleaf + vanilla JavaScript + vanilla CSS. KHÔNG dùng
  React / Vue / Angular / framework JS. KHÔNG dùng Tailwind.
- **Database:** MySQL 8, Flyway (bắt đầu từ V1 của app này), JDBC thuần
  qua `DataSetRegistry` / `DataSet`.
- **Chạy:** Docker Compose, 2 container `app` + `mysql`.
  Lệnh chuẩn: `docker compose up --build`
- **Âm thanh:** SpeechSynthesis của trình duyệt (giọng vi-VN) + Web Audio
  API tạo tiếng bíp. KHÔNG dùng file mp3.
- **Build:** `mvn test && mvn clean package`
- **Health:** `GET /actuator/health` phải trả 200 khi app chạy.

### 2.1. CẤM TUYỆT ĐỐI

- CẤM Repository / JpaRepository / CrudRepository / Entity / Hibernate /
  JdbcTemplate / EntityManager.
- CẤM đưa business vào Controller. CẤM đưa business vào trx.
- CẤM hardcode menu, hardcode quyền, hardcode URL.
- CẤM hardcode điểm trừ, điểm liệt, giờ thi trong code Java/JS.
  Tất cả phải đọc từ DB (`SH_ERROR_RULE`, `SH_STEP`, `SH_LESSON`).
- CẤM tự đổi định nghĩa 3 luồng thi ở §4. CẤM gộp Luồng 2 và Luồng 3.
- CẤM tự thêm / bớt / đổi thứ tự 14 mục thi.
- Nếu convention Java thông thường xung đột với Skills thì ưu tiên Skills.

### 2.2. Kiến trúc module — triad bắt buộc

Mỗi function phải đủ 4 file, không được thiếu:

```
src/main/java/com/sahinh/io/vn/controller/<SYSTEM>/<SUBSYSTEM>/<FUNCTION>/<FUNCTION>_pg.java
src/main/java/com/sahinh/io/vn/api/<SYSTEM>/<SUBSYSTEM>/<FUNCTION>/<FUNCTION>_trx.java
src/main/java/com/sahinh/io/vn/api/<SYSTEM>/<SUBSYSTEM>/<FUNCTION>/<FUNCTION>_mod.java
src/main/resources/templates/<SYSTEM>/<SUBSYSTEM>/<FUNCTION>/<FUNCTION>.html
```

- `*_pg.java` (Controller): chỉ mapping URL + trả template. Không
  nghiệp vụ, không query DB, không kiểm tra quyền, không validate.
- `*_trx.java` (Transaction API): chỉ nhận request, validate cơ bản,
  gọi mod, trả JSON `{code, message, data}`. Không viết business.
- `*_mod.java` (Business Module): toàn bộ nghiệp vụ + SQL qua
  `DataSetRegistry`. Logic dài thì chia private method.
- Logic thi (đếm giờ, chuyển bước, tính điểm, xử lý liệt) tập trung
  trong **1 engine duy nhất `ExamEngine`** dùng chung cho 3 luồng.
  Không copy logic tính điểm ra 3 nơi.

URL:
- Trang: `/{SYSTEM_CODE}/{FUNCTION_CODE}` — ví dụ `/SH/SHA0_0400`
- API: `/api/{SYSTEM_CODE}/{SUBSYSTEM_CODE}/{FUNCTION_CODE}/...`
  — ví dụ `/api/SH/A0/SHA0_0400/prompt?TxRequest=true`

---

## 3. KIẾN THỨC NGHIỆP VỤ — LUẬT THI SA HÌNH THẬT (ĐỂ LÀM MẶC ĐỊNH)

Dùng bảng này làm dữ liệu seed mặc định. Sau này thầy/superadmin có
thể sửa trong cấu hình, nhưng code không được hardcode nó.

- Tổng: 14 mục liên hoàn (11 bài + 3 lần qua ngã tư xen giữa — xe qua
  ngã tư 4 lần trong sân, mỗi lần chấm riêng), tổng thời gian chuẩn
  **18 phút (1080 giây)**.
- Bắt đầu **100 điểm**, đạt khi còn **từ 80 điểm** trở lên.
- Chấm tự động bằng cảm biến ở thi thật; ở app này thầy chấm tay.
- Tốc độ trong sân tối đa **24 km/h** (trừ bài thay đổi số).
  Vòng tua máy tối đa **4.000 vòng/phút**.
- Quá thời gian quy định: cứ **3 giây trừ 1 điểm**.
- Chết máy: **-5 điểm/lần**. Đè vạch: **-5 điểm/lần**.
  Vượt đèn đỏ: **-10 điểm**.
- Thi chính thức loa **CHỈ đọc tên bài**, ví dụ "Bài thi xuất phát",
  **KHÔNG đọc bước nhỏ**. Bước nhỏ chỉ là cách mình tách ra để luyện.
- Từ 01/07/2026: phải đỗ lý thuyết mới được thi sa hình, đỗ sa hình
  mới được thi đường trường. App này không cần kiểm tra lý thuyết.

### 3.1. 14 mục thi chuẩn — thứ tự cố định

| Mục | Tên mục | Giờ chuẩn | Lời loa mặc định |
|---:|---|---|---|
| 1 | Xuất phát | 20 giây để qua vạch | Bài thi xuất phát |
| 2 | Dừng xe nhường đường cho người đi bộ | dừng 1 lần | Bài thi dừng xe nhường đường cho người đi bộ |
| 3 | Dừng xe và khởi hành ngang dốc | 30 giây để qua dốc | Bài thi dừng xe và khởi hành ngang dốc |
| 4 | Qua vệt bánh xe và đường hẹp vuông góc | 2 phút | Bài thi qua vệt bánh xe và đường hẹp vuông góc |
| 5 | Qua ngã tư (1) | 20 giây khi đèn xanh | Bài thi qua ngã tư có đèn tín hiệu |
| 6 | Đường vòng quanh co (chữ S) | 2 phút | Bài thi đường vòng quanh co |
| 7 | Qua ngã tư (2) | 20 giây khi đèn xanh | Bài thi qua ngã tư có đèn tín hiệu |
| 8 | Ghép xe dọc vào nơi đỗ | 2 phút | Bài thi ghép xe dọc vào nơi đỗ |
| 9 | Qua ngã tư (3) | 20 giây khi đèn xanh | Bài thi qua ngã tư có đèn tín hiệu |
| 10 | Tạm dừng ở chỗ có đường sắt chạy qua | dừng 1 lần | Bài thi tạm dừng ở chỗ có đường sắt chạy qua |
| 11 | Thay đổi số trên đường bằng | 2 phút | Bài thi thay đổi số trên đường bằng |
| 12 | Ghép xe ngang vào nơi đỗ | 2 phút | Bài thi ghép xe ngang vào nơi đỗ |
| 13 | Qua ngã tư (4) | 20 giây khi đèn xanh | Bài thi qua ngã tư có đèn tín hiệu |
| 14 | Kết thúc | — | Bài thi kết thúc |

Bài 11: xe số tự động không thi. Khi chọn xe số tự động thì bỏ bài 11.

> S12 (26/11): trước đây gộp 4 lần qua ngã tư thành 1 bài nên chỉ có
> 11 bài; user gửi ảnh màn hình CÀI ĐẶT CHỨC NĂNG chính thức (14 mục,
> mỗi ngã tư: quá 20 giây -5đ, quá 30 giây loại trực tiếp) nên tách ra
> thành 14 mục, LESSON_NO = số thứ tự chính thức (V5).

### 3.2. 29 bước nhỏ mặc định (chỉ dùng cho Luồng 1 và 2)

Đây là danh sách seed (khớp V2 + V5). Admin chỉ sửa số giây / bật-tắt,
không cần tự tạo mới. 3 ngã tư (2)/(3)/(4) có cùng 2 bước như ngã tư (1).

| Mục | Bước nhỏ | Lời loa khi bật | Giây mặc định |
|---|---|---|---:|
| 1 | 1.1 Thắt dây an toàn | Thắt dây an toàn | 10 |
| 1 | 1.2 Bật xi nhan trái và xuất phát | Bật xi nhan trái, xuất phát | 20 |
| 1 | 1.3 Tắt xi nhan trái sau 5m | Tắt xi nhan trái | 5 |
| 2 | 2.1 Dừng đúng vạch người đi bộ | Dừng xe nhường đường cho người đi bộ | 15 |
| 3 | 3.1 Dừng đúng vạch trên dốc | Dừng xe trên dốc | 15 |
| 3 | 3.2 Khởi hành qua dốc | Khởi hành qua dốc | 30 |
| 4 | 4.1 Bánh trước qua vệt bánh xe | Qua vệt bánh xe | 60 |
| 4 | 4.2 Bánh sau qua vệt bánh xe | Qua vệt bánh xe | 30 |
| 4 | 4.3 Qua đường hẹp vuông góc | Qua đường hẹp vuông góc | 30 |
| 5 | 5.1 Dừng / đi theo đèn tín hiệu | Qua ngã tư có đèn tín hiệu | 20 |
| 5 | 5.2 Rẽ qua ngã tư | Qua ngã tư có đèn tín hiệu | 20 |
| 6 | 6.1 Vào đường vòng quanh co | Qua đường vòng quanh co | 60 |
| 6 | 6.2 Ra đường vòng quanh co | Qua đường vòng quanh co | 60 |
| 7 | 7.1 Dừng / đi theo đèn tín hiệu | Qua ngã tư có đèn tín hiệu | 20 |
| 7 | 7.2 Rẽ qua ngã tư | Qua ngã tư có đèn tín hiệu | 20 |
| 8 | 8.1 Đưa xe vào miệng chuồng dọc | Ghép xe dọc vào nơi đỗ | 40 |
| 8 | 8.2 Lùi xe vào chuồng dọc | Ghép xe dọc vào nơi đỗ | 40 |
| 8 | 8.3 Chỉnh thẳng xe và ra khỏi chuồng | Ghép xe dọc vào nơi đỗ | 40 |
| 9 | 9.1 Dừng / đi theo đèn tín hiệu | Qua ngã tư có đèn tín hiệu | 20 |
| 9 | 9.2 Rẽ qua ngã tư | Qua ngã tư có đèn tín hiệu | 20 |
| 10 | 10.1 Dừng đúng vạch đường sắt | Dừng xe ở chỗ có đường sắt chạy qua | 15 |
| 11 | 11.1 Tăng số trên đường bằng | Tăng số trên đường bằng | 60 |
| 11 | 11.2 Giảm số trên đường bằng | Giảm số trên đường bằng | 60 |
| 12 | 12.1 Tiến xe song song ô đỗ ngang | Ghép xe ngang vào nơi đỗ | 40 |
| 12 | 12.2 Lùi chéo vào ô đỗ ngang | Ghép xe ngang vào nơi đỗ | 40 |
| 12 | 12.3 Trả lái thẳng xe trong ô | Ghép xe ngang vào nơi đỗ | 40 |
| 13 | 13.1 Dừng / đi theo đèn tín hiệu | Qua ngã tư có đèn tín hiệu | 20 |
| 13 | 13.2 Rẽ qua ngã tư | Qua ngã tư có đèn tín hiệu | 20 |
| 14 | 14.1 Bật xi nhan phải và kết thúc | Bật xi nhan phải, kết thúc | 10 |

### 3.3. Bảng lỗi mặc định — cấu hình được

Mỗi lỗi có: `CODE`, `NAME`, `DEDUCT_POINTS`, `IS_LIET (0/1)`.

- `IS_LIET = 0`: bấm `-` thì trừ điểm, đi tiếp.
- `IS_LIET = 1`: bấm `-` thì dừng thi ngay, hiện **RỚT + lý do**.

| Lỗi | Trừ | Liệt | Bài |
|---|---:|---:|---|
| Không thắt dây an toàn | 5 | 0 | 1 |
| Không bật xi nhan trái lúc xuất phát | 5 | 0 | 1 |
| Không tắt xi nhan sau 5m | 5 | 0 | 1 |
| Quá 20s chưa qua vạch xuất phát | 5 | 0 | 1 |
| Quá 30s chưa xuất phát | — | 1 | 1 |
| Không dừng ở vạch người đi bộ / đường sắt | 5 | 0 | 2, 8 |
| Dừng cách vạch quá 500mm | 5 | 0 | 2, 3, 8 |
| Dừng quá vạch | 5 | 0 | 2, 3, 8 |
| Không dừng ở vạch dốc | — | 1 | 3 |
| Tụt dốc quá 500mm | — | 1 | 3 |
| Quá 30s chưa qua dốc | — | 1 | 3 |
| **Tắt máy trên dốc** | — | **1 — user chốt, khác luật thật (-5)** | **3** |
| Chết máy (bài khác) | 5/lần | 0 | Chung |
| Bánh xe đè vạch | 5/lần | 0 | 4, 6, 8, 12 |
| Bánh xe ra ngoài hình bài | — | 1 | 4, 6 |
| Vượt đèn đỏ | 10 | 0 | 5, 7, 9, 13 |
| Không bật xi nhan khi rẽ | 5 | 0 | 5, 7, 9, 13 |
| Quá 20 giây chưa qua ngã tư | 5 | 0 | 5, 7, 9, 13 |
| Quá 30 giây chưa qua ngã tư | — | 1 | 5, 7, 9, 13 |
| Quá 2 phút (bài 4/6/8/11/12) | 5 | 0 | 4, 6, 8, 11, 12 |
| Hết giờ ghép xe mà chưa vào hẳn ô | — | 1 | 8, 12 |
| Không tăng / không giảm số đúng | 5 | 0 | 11 |
| Không bật xi nhan phải lúc kết thúc | 5 | 0 | 14 |
| Lái xe lên vỉa hè / gây tai nạn | — | 1 | Chung |
| Quá tổng thời gian 18 phút | 1 / 3 giây | 0 | Chung (Luồng 2/3) |

---

## 4. 3 LUỒNG THI — PHẦN QUAN TRỌNG NHẤT, KHÔNG ĐƯỢC HIỂU SAI

### Luồng 1 — Thi chuẩn, tự động hoàn toàn (AUTO) — `SHA0_0400`

- Thầy chỉ bấm **Bắt đầu 1 lần duy nhất**.
- Sau đó hệ thống **TỰ** đếm giờ, **TỰ** đọc loa, **TỰ** chuyển bước,
  **TỰ** chạy tới Kết thúc rồi hiện điểm. Thầy **KHÔNG** bấm chuyển bước.
- Có bước nhỏ chi tiết (~29 bước).
- **Sai ở bước nào thì DỪNG NGAY ở bước đó và LÀM LẠI TỪ ĐẦU.**
  Không trừ điểm rồi đi tiếp. Đây là chế độ ép phải đúng.
- **Thời gian từng bước/bài do thầy/admin tự điền bằng giây.**
  Mỗi thầy có khoảng thời gian cho phép khác nhau, họ tự biết tự tính.
  **HỆ THỐNG KHÔNG ÉP đúng luật thi thật, KHÔNG cảnh báo tổng > 18 phút,
  KHÔNG bắt phải đúng 20s / 30s / 2 phút.** Thầy nhập bao nhiêu thì máy
  đếm bấy nhiêu. Không được thêm validation tổng thời gian.
- Học viên bấm "Hoàn thành bước" trước khi hết giờ thì qua bước tiếp.
  Hết giờ chưa xong thì coi như sai -> dừng + làm lại từ đầu.
- Kết thúc: hiện ĐẠT (100 điểm / hoàn thành đúng giờ) hoặc TRƯỢT ở
  bước nào + lý do + nút "Làm lại từ đầu".

### Luồng 2 — Bấm chay, chấm điểm theo bước nhỏ (MANUAL CHI TIẾT) — `SHA0_0500`

- Thầy bấm Bắt đầu, rồi **TỰ bấm** để qua bước tiếp theo.
- Mỗi bước nhỏ thầy bấm **+** nếu học viên đúng, bấm **-** nếu sai.
- Khi bấm `-`, màn hình hiện danh sách lỗi của đúng bước/bài đó để
  thầy chọn lỗi cụ thể (ví dụ "Dừng xa vạch quá 500mm -5",
  "Tụt dốc quá 500mm - Liệt"). Không được trừ chung chung -5.
- Máy tự cộng/trừ điểm theo bảng lỗi đã cấu hình. Bắt đầu 100 điểm.
- Có bước nhỏ chi tiết (~29 bước), giống Luồng 1 về danh sách bước.
- Chạy hết toàn bộ bài thi thì hiện điểm + ĐẠT (>=80) / TRƯỢT (<80).
- Lỗi liệt: bấm `-` vào lỗi liệt thì dừng ngay, hiện RỚT + lý do.

### Luồng 3 — Bấm chay, thi thật, không bước nhỏ (MANUAL CHUẨN) — `SHA0_0600`

- **KHÔNG có bước nhỏ chi tiết.** Chỉ có đúng 14 mục chính thức.
- Loa chỉ đọc tên bài, ví dụ "Bài thi xuất phát" rồi đếm 20 giây, xong.
- Thầy **PHẢI bấm** mới qua bài tiếp theo. Không tự chuyển như Luồng 1.
- Thầy cũng bấm **+ / -** như Luồng 2 để tính điểm (bấm `-` thì chọn
  lỗi của bài đó, giống Luồng 2).
- **KHÔNG chỉnh sửa gì trong luồng này.** Giờ và điểm lấy theo chuẩn
  đã nhập sẵn trong preset đang áp dụng.
- Lỗi liệt: giống Luồng 2.

### Bảng phân biệt nhanh — dán vào code review để khỏi nhầm lại

|  | Luồng 1 | Luồng 2 | Luồng 3 |
|---|---|---|---|
| Tự chuyển bước? | Có, tự động | Không, thầy bấm | Không, thầy bấm |
| Bước nhỏ? | Có (~29) | Có (~29) | Không, chỉ 14 mục |
| Bấm + / - ? | Không | Có | Có |
| Chỉnh được giây? | Có, tự do | Có, theo preset | Không |
| Sai thì sao? | Dừng, làm lại từ đầu | Trừ điểm, đi tiếp (trừ liệt) | Trừ điểm, đi tiếp (trừ liệt) |
| Loa đọc gì? | Từng bước nhỏ (nếu bật) hoặc tên bài (nếu tắt) | Từng bước nhỏ / tên bài | Chỉ tên bài |

**Nếu user nói "làm thi" mà không nói luồng nào thì HỎI luồng 1/2/3,
không tự chọn.**

---

## 5. NÚT BẬT/TẮT BƯỚC NHỎ

Thi chính thức không đọc bước nhỏ. Bước nhỏ chỉ để luyện.

- Có 2 lớp công tắc:
  1. Công tắc tổng: "Hướng dẫn chi tiết" (bật/tắt toàn bộ bước nhỏ).
  2. Công tắc riêng từng bước nhỏ.
- Khi **TẮT**: bỏ qua bước nhỏ, chỉ dùng thời gian + lời loa của bài cha.
  Ví dụ bài 1 tắt thì chỉ đọc "Bài thi xuất phát" rồi đếm 20 giây, xong.
- Khi **BẬT**: đọc lần lượt từng bước nhỏ, đếm giờ từng bước.
  Ví dụ bài 1 bật thì đọc: "Thắt dây an toàn" 10s -> "Bật xi nhan trái,
  xuất phát" 20s -> "Tắt xi nhan trái" 5s.
- Mỗi bài lưu RIÊNG thời gian tổng của bài (dùng khi tắt), mỗi bước nhỏ
  lưu RIÊNG thời gian của nó (dùng khi bật). Không cộng dồn sai.

---

## 6. PHÂN QUYỀN VÀ PRESET — SUPERADMIN SETUP SẴN, THẦY ÁP DỤNG

Role:
- `SUPERADMIN`: setup sẵn bộ cấu hình mẫu (preset), quản lý user.
- `TEACHER`: dùng preset hoặc tự setup riêng.
- `STUDENT` (nếu có): chỉ được thi, không được sửa cấu hình.

Luồng preset — bắt buộc làm đúng:
1. Superadmin tạo preset: tên preset, thời gian từng bài/bước, bật/tắt
   bước nhỏ, bảng lỗi + điểm trừ + cờ liệt.
2. Teacher vào danh sách preset, bấm **1 nút ÁP DỤNG** là dùng luôn bộ
   của Superadmin cho phiên thi của mình.
3. Thầy nào muốn khác thì bấm **TỰ SETUP RIÊNG**. Hệ thống **COPY**
   preset ra 1 bản riêng cho thầy đó tự chỉnh. **KHÔNG được ghi đè
   lên bản gốc của Superadmin.** Bản riêng chỉ thầy đó thấy và dùng.
4. Không được để teacher sửa trực tiếp preset gốc của Superadmin.
   Kiểm tra `OWNER_ID` / `IS_TEMPLATE` trong mod `SHA0_0300`.

Menu/quyền: đọc từ `SYS_FUNCTION`, kiểm tra quyền chỉ ở Filter
(`SYS_FUNCTION -> SYS_ROLE_USER -> SYS_ROLE_FUNCTION -> PASS`).
Không kiểm tra quyền trong controller/mod (trừ kiểm tra owner preset ở trên).

---

## 7. DANH SÁCH FUNCTION PHẢI LÀM

| Function | Tên | Hệ | Ghi chú |
|---|---|---|---|
| `ZZA0_0001` | Đăng nhập / session | ZZ | BCrypt, session, role |
| `MSA0_0100` | Dashboard | MS | Số phiên thi, tỉ lệ đậu |
| `MSA0_0200` | Người dùng, role, quyền | MS | Menu động |
| `SHA0_0100` | Cấu hình Bài & Bước nhỏ | SH | Sửa tên, lời loa, giây, bật/tắt |
| `SHA0_0200` | Cấu hình Lỗi & Điểm liệt | SH | Sửa điểm trừ, cờ liệt |
| `SHA0_0300` | Mẫu cấu hình (Preset) | SH | Tạo preset, Áp dụng, Tự setup riêng |
| `SHA0_0400` | Thi Luồng 1 — Tự động | SH | Chỉ nút Bắt đầu, tự chạy |
| `SHA0_0500` | Thi Luồng 2 — Bấm chay chi tiết | SH | Nút Next / + / - theo bước nhỏ |
| `SHA0_0600` | Thi Luồng 3 — Bấm chay thi thật | SH | Nút Next / + / - theo 14 mục |
| `SHA0_0700` | Kết quả & Lịch sử phiên thi | SH | Điểm, đạt/trượt, bước trượt, lý do |

---

## 8. DATABASE — FLYWAY V1–V3

Database: `sahinh_b2`, MySQL 8, Docker container `mysql`.

| Migration | Nội dung |
|---|---|
| `V1__create_foundation_auth.sql` | `SYS_USER`, `SYS_ROLE`, `SYS_ROLE_USER`, `SYS_FUNCTION`, `SYS_ROLE_FUNCTION`, `SYS_FUNCTION_PAGE`, seed SUPERADMIN/TEACHER |
| `V2__create_exam_config.sql` | `SH_PRESET`, `SH_LESSON`, `SH_STEP`, `SH_ERROR_RULE`, seed 11 bài + bước nhỏ §3.2 + bảng lỗi §3.3 |
| `V3__create_exam_session.sql` | `SH_SESSION`, `SH_SESSION_LOG` |
| `V4__add_transmission_and_dangerous.sql` | `SH_SESSION.TRANSMISSION`, `SH_PRESET.DANGEROUS_ENABLED` / `DANGEROUS_TIMEOUT_SECONDS`, lỗi `DANGEROUS` |
| `V5__add_intersection_crossings.sql` | 11 bài -> 14 mục (thêm ngã tư (2)/(3)/(4), đổi LESSON_NO 7->8, 8->10, 9->11, 10->12, 11->14), 29 bước, lỗi quá giờ ngã tư |

Bảng chính:
- `SH_PRESET (ID, NAME, OWNER_ID, IS_TEMPLATE, IS_ACTIVE, CREATED_AT)`
- `SH_LESSON (ID, PRESET_ID, LESSON_NO 1-14, NAME, ANNOUNCE_TEXT, DURATION_SECONDS, SORT_ORDER)`
- `SH_STEP (ID, LESSON_ID, STEP_NO, NAME, ANNOUNCE_TEXT, DURATION_SECONDS, ENABLED, SORT_ORDER)`
- `SH_ERROR_RULE (ID, PRESET_ID, LESSON_ID, CODE, NAME, DEDUCT_POINTS, IS_LIET, SORT_ORDER)`
- `SH_SESSION (ID, USER_ID, PRESET_ID, FLOW 1/2/3, SCORE, PASSED, FAILED_STEP, FAILED_REASON, STARTED_AT, ENDED_AT)`
- `SH_SESSION_LOG (ID, SESSION_ID, LESSON_NO, STEP_NO, RESULT, DEDUCT_POINTS, NOTE, CREATED_AT)`

Không tạo migration mới nếu không có thay đổi schema thực sự.
Migration phải idempotent khi có thể (kiểm tra cột đã tồn tại chưa
rồi mới ALTER — MySQL không có `ADD COLUMN IF NOT EXISTS`).

---

## 9. GIAO DIỆN — MÀN HÌNH BẮT BUỘC

### 9.1. Màn hình chọn luồng (trang chủ sau login)
3 card lớn, mỗi card ghi rõ tên luồng + mô tả 1 dòng + nút Bắt đầu:
- Luồng 1 — Thi chuẩn tự động — "Bấm bắt đầu 1 lần, máy tự chạy hết"
- Luồng 2 — Bấm chay chi tiết — "Thầy bấm + / - theo từng bước nhỏ"
- Luồng 3 — Bấm chay thi thật — "Chỉ 14 mục, thầy bấm mới qua bài"
Thêm ô chọn preset đang áp dụng + tên học viên (nhập tay).

### 9.2. Màn hình thi (dùng chung layout cho 3 luồng)
- Đồng hồ lớn ở giữa (giây còn lại của bước/bài hiện tại), đổi màu đỏ
  khi còn <= 5 giây.
- Tên bài hiện tại + tên bước nhỏ hiện tại (nếu có).
- Lời loa đang đọc (hiện chữ luôn, phòng khi loa không đọc được).
- Điểm còn lại lớn, rõ: `100 -> 95 -> ...`, thanh tiến trình 14 mục.
- Nút Tắt tiếng / Bật loa.
- Luồng 1: chỉ có nút Bắt đầu / Hoàn thành bước / Làm lại từ đầu.
- Luồng 2/3: nút to, dễ bấm khi đang đứng ngoài sân: **+ Đúng** (xanh),
  **- Sai** (đỏ, bấm xong hiện list lỗi để chọn), **Qua bài/bước**,
  **Kết thúc sớm**.
- Khi bấm `-` vào lỗi liệt: hiện ngay màn hình đỏ **RỚT — <lý do>**,
  nút "Làm lại từ đầu" + "Xem kết quả".
- Mobile phải dùng được: nút to, chữ to, không cần cuộn ngang.

### 9.3. Màn hình cấu hình (Superadmin / Teacher)
- Bảng 14 mục: mỗi dòng có tên, lời loa (input text), giây (input number),
  công tắc bật/tắt bước nhỏ.
- Mở rộng 1 bài thì thấy list bước nhỏ bên trong, mỗi bước có lời loa,
  giây, công tắc riêng.
- Bảng lỗi: mỗi dòng có tên lỗi, điểm trừ, checkbox Liệt.
- Màn hình preset: list preset, nút Áp dụng, nút Tự setup riêng,
  nút Tạo preset mới (chỉ Superadmin).

### 9.4. Màn hình kết quả
- Điểm lớn, chữ ĐẠT (xanh) / TRƯỢT (đỏ) / RỚT DO LỖI LIỆT (đỏ).
- List các lỗi đã mắc: bước nào, lỗi gì, trừ mấy điểm, lúc nào.
- Nút "Thi lại" + "Xem lịch sử".

---

## 10. ÂM THANH

- Đọc loa: `speechSynthesis.speak(new SpeechSynthesisUtterance(text))`,
  `lang = 'vi-VN'`. Câu đọc lấy từ DB (`ANNOUNCE_TEXT`), không hardcode.
- Tiếng bíp khi qua vạch / nhận bài: Web Audio API tạo 1 nốt ngắn
  (ví dụ 880Hz, 0.15s). Không dùng file âm thanh.
- Trình duyệt chặn autoplay: lần bấm Bắt đầu đầu tiên là gesture mở
  khóa âm thanh. Phải có nút Tắt tiếng.
- Nếu SpeechSynthesis không có giọng vi-VN thì vẫn hiện chữ lời loa
  lên màn hình, không được crash.

---

## 11. THỨ TỰ BUILD — S01 ĐẾN S07, KHÔNG NHẢY BƯỚC

| Session | Làm gì | Done khi nào |
|---|---|---|
| S01 | Foundation + Auth + Docker | `docker compose up --build` chạy được, login được, menu động, `PROJECT_INDEX.md` + `docs/features/` đã tạo, Flyway V1, health 200 |
| S02 | Cấu hình thi | Superadmin tạo được preset, sửa được giây/bước/lỗi/liệt, teacher bấm Áp dụng được, Tự setup riêng copy ra bản riêng không ghi đè gốc |
| S03 | Luồng 1 — Tự động + `ExamEngine` | Bấm Bắt đầu 1 lần, máy tự đọc loa + đếm giờ + chuyển bước tới Kết thúc, sai thì dừng + làm lại từ đầu, hiện điểm |
| S04 | Luồng 2 — Bấm chay chi tiết | Thầy bấm Next / + / - theo từng bước nhỏ, điểm tự trừ đúng bảng lỗi, lỗi liệt dừng ngay hiện RỚT |
| S05 | Luồng 3 — Bấm chay thi thật | Chỉ 14 mục, không bước nhỏ, thầy phải bấm mới qua bài, bấm + / -, không chỉnh được giờ/điểm trong luồng này |
| S06 | Kết quả & Lịch sử | Hiện điểm, đạt/trượt, bước trượt + lý do, lưu lịch sử phiên thi, xem lại được |
| S07 | Polish | Âm thanh loa + bíp ổn, nút Tắt tiếng, mobile dùng được, test lại 3 luồng, cập nhật docs |

Không nhảy sang Luồng 2/3 khi Luồng 1 + `ExamEngine` chưa xong.

---

## 12. KIỂM THỬ — PHẢI PASS MỚI ĐƯỢC NÓI "XONG"

Trước khi báo xong mỗi session, chạy và ghi kết quả vào report:

1. `mvn test` — exit 0.
2. `mvn clean package` — exit 0, ra JAR.
3. `docker compose up --build` — app lên, `curl /actuator/health` = 200.
4. Chạy tay 3 luồng:
   - Luồng 1: bấm Bắt đầu, để hết giờ 1 bước -> phải dừng + làm lại từ đầu.
     Hoàn thành hết -> hiện điểm.
   - Luồng 2: bấm `-` chọn lỗi "Dừng xa vạch quá 500mm" -> điểm trừ đúng 5.
     Bấm `-` chọn lỗi liệt "Tắt máy trên dốc" -> hiện RỚT ngay.
   - Luồng 3: kiểm tra chỉ có 14 mục, không có bước nhỏ, thầy không bấm
     thì không tự qua bài. Bấm `+` / `-` tính điểm đúng.
   - Preset: teacher bấm Áp dụng preset Superadmin -> dùng đúng giờ/điểm
     của preset đó. Bấm Tự setup riêng -> sửa bản riêng không đổi bản gốc.
   - Bật/tắt bước nhỏ: tắt -> bài 1 chỉ đọc "Bài thi xuất phát" đếm 20s.
     Bật -> bài 1 đọc 3 bước nhỏ.
5. Kiểm tra cấm: `grep -r "JpaRepository\|JdbcTemplate\|@Entity" src/` phải
   không có kết quả (trừ comment giải thích cấm).

Nếu 1 mục fail thì không được nói "xong", phải sửa rồi test lại.

---

## 13. OUTPUT YÊU CẦU SAU MỖI SESSION

- Code + migration + HTML đủ triad, không thiếu file.
- Report `docs/features/<FEATURE>/report/report_session_0X.md` ghi:
  ngày, function, thay đổi, file đã sửa, kết quả test (lệnh + exit code),
  bằng chứng chạy tay 3 luồng (nếu liên quan).
- Cập nhật `ROADMAP.md` §3 (trạng thái) + §10 (append session mới).
- Cập nhật `PROJECT_INDEX.md` nếu thêm/đổi module.
- Không xóa lịch sử trong Skills/Roadmap. Chỉ append.

---

## 14. KHI THIẾU THÔNG TIN THÌ HỎI, KHÔNG ĐOÁN

- Thiếu `SYSTEM_CODE` / `SUBSYSTEM` / `FUNCTION_CODE` thì hỏi.
- User nói "làm thi" mà không nói luồng thì hỏi luồng 1/2/3.
- User nói "điểm liệt" mà không nói lỗi nào thì hỏi lỗi nào liệt.
- Không tự đổi giờ chuẩn, điểm trừ, thứ tự bài nếu user chưa chốt.

---

*Prompt này được tổng hợp ngày 2026-11-23 từ toàn bộ trao đổi với user
+ `skills/#SAHINH-B2—SKILLS.txt` + `ROADMAP.md`. Nếu 3 tài liệu này
mâu thuẫn nhau thì ưu tiên: Skills > Prompt này > Roadmap, và hỏi user
trước khi code phần mâu thuẫn đó.*

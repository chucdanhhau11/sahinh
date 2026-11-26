# Report Session 12 — 2026-11-26 — Trình tự thi chính thức 14 mục (thêm 3 lần qua ngã tư)

## A. User yêu cầu

- 15:50: user bảo liệt kê lại toàn bộ các bước, "hình như thiếu rồi".
- 16:12: user gửi ảnh màn hình **CÀI ĐẶT CHỨC NĂNG** (màn hình chính thức
  của hệ thống thi): 14 mục, trong đó "Qua ngã tư" xuất hiện **4 lần**
  (ngã tư (1)..(4)), mỗi lần đều có "Quá 20 giây (-5đ)" và
  "Quá 30 giây loại trực tiếp".

## B. Đối chiếu — app thiếu 3 mục

| App trước S12 (V2) | Màn hình chính thức |
|---|---|
| 11 bài, 23 bước | **14 mục** |
| "Qua ngã tư" chỉ 1 bài (bài 5) | Qua ngã tư (1), (2), (3), (4) — chấm riêng từng lần |

Nguyên nhân: "11 bài" là cách gọi gộp trong tài liệu; trên sân, xe qua
ngã tư 4 lần (sau vệt bánh xe, sau đường vòng, sau ghép dọc, sau ghép
ngang) và chip chấm riêng mỗi lần qua. V2 gộp cả 4 lần thành 1 bài nên
thiếu 3 mục — đúng như user nghi.

Thứ tự chính thức (theo ảnh user gửi):

1. Xuất phát (quá 20s -5đ, quá 30s loại)
2. Dừng xe nhường đường cho người đi bộ
3. Dừng và khởi hành xe ngang dốc (quá 30s loại)
4. Qua vệt bánh xe và đường vòng vuông góc (quá 2 phút -5đ)
5. Qua ngã tư (1) (quá 20s -5đ, quá 30s loại)
6. Qua đường vòng quanh co (quá 2 phút -5đ)
7. Qua ngã tư (2) (quá 20s -5đ, quá 30s loại)
8. Ghép xe dọc vào nơi đỗ (quá 2 phút -5đ)
9. Qua ngã tư (3) (quá 20s -5đ, quá 30s loại)
10. Tạm dừng ở đường sắt
11. Thay đổi số trên đường bằng (quá 2 phút -5đ)
12. Ghép xe ngang vào nơi đỗ (quá 2 phút -5đ)
13. Qua ngã tư (4) (quá 20s -5đ, quá 30s loại)
14. Kết thúc

Lưu ý: ảnh ghi mục 4 là "đường vòng vuông góc" nhưng tên chuẩn theo
Bộ GTVT là "đường hẹp vuông góc" — giữ tên cũ của app, không đổi theo
ảnh (user chỉ báo thiếu bước, không báo đổi tên).

## C. Thiết kế sửa (đã chốt trong session)

`LESSON_NO` trở thành **số thứ tự chính thức 1–14** (trùng số hiển thị
"Bài N" trên màn hình thi). Không giữ LESSON_NO 1–11 rồi xen bằng
`SORT_ORDER`, vì như vậy "Bài N" hiển thị sai và lỗi theo bài
(`listErrors` lọc theo `LESSON_NO`) phải nhân bản theo số giả.

Đổi số: 7 Ghép dọc -> 8, 8 Đường sắt -> 10, 9 Thay đổi số -> 11,
10 Ghép ngang -> 12, 11 Kết thúc -> 14. Thêm mới: 7 ngã tư (2),
9 ngã tư (3), 13 ngã tư (4). Bài 5 đổi tên "Qua ngã tư (1)".
Lời loa 3 ngã tư mới = câu chung "Bài thi qua ngã tư có đèn tín hiệu"
(nên không cần file mp3 mới — dùng chung lesson-05.mp3).

Bước nhỏ: mỗi ngã tư mới có cùng 2 bước như ngã tư (1) (theo đèn + rẽ)
-> tổng bước 23 + 6 = **29 bước**.

Lỗi: nhân bản Vượt đèn đỏ (-10) + Không xi nhan khi rẽ (-5) cho 7/9/13;
thêm mới "Quá 20 giây chưa qua ngã tư" (-5) và "Quá 30 giây chưa qua
ngã tư" (liệt) cho cả 4 lần qua (5, 7, 9, 13) — trước S12 không có 2
lỗi quá giờ nào cho ngã tư (bài 1 và bài 3 đã có sẵn dạng này).
Tổng lỗi: 24 (V2) + 1 DANGEROUS (V4) + 14 mới = **39 lỗi**.
Quy tắc quá 2 phút (-5đ) đã có sẵn dạng lỗi chung (`OVERTIME_2MIN`,
`LESSON_NO=0`) nên không thêm.

Chỉ sửa preset mẫu id=1 ("Chuẩn thi thật"). Preset đã copy trước V5
giữ nguyên 11 bài (superadmin copy lại từ mẫu nếu muốn 14 mục).

## D. Thay đổi code

- **Mới: `V5__add_intersection_crossings.sql`** — toàn DML chuẩn SQL
  (chạy được cả MySQL lẫn H2): UPDATE đổi số có WHERE kèm NAME (mỗi
  lệnh trúng đúng 1 bài, chạy lại không đổi gì); INSERT bài/bước/lỗi
  dạng `SELECT ... WHERE NOT EXISTS` như V4. Không sửa V1–V4.
- `ExamEngine.java`: số tự động bỏ bài `9` -> **`11`** (Thay đổi số).
- `exam.js`: `LESSON_AUDIO` mở rộng 14 mục — 7/9/13 -> `lesson-05.mp3`,
  8 -> `lesson-07.mp3`, 10 -> `lesson-08.mp3`, 11 -> `lesson-09.mp3`,
  12 -> `lesson-10.mp3`, 14 -> `lesson-11.mp3`. `AUDIO_MAP` không đổi
  (khóa theo lời loa, ngã tư chung 1 câu nên tự đúng).
- Template: 3 màn hình thi "bỏ bài 9" -> "bỏ bài 11"; dashboard
  "Chỉ 11 bài" -> "Chỉ 14 mục", "~23 bước" -> "~29 bước";
  SHA0_0500/SHA0_0600 tương tự; comment `SHA0_0600_mod`.
- `ExamEngineTest.java`: test số tự động đổi fixture 8/9/10 ->
  10/11/12, assert bỏ bài 11.
- Docs: `PROMPT.md` §3.1 (bảng 14 mục), §3.2 (bảng 29 bước đầy đủ theo
  seed — bảng cũ chỉ liệt kê 16 dòng gộp), §3.3 (số bài trong bảng lỗi
  + 2 lỗi quá giờ ngã tư), §8 (thêm V4/V5, `LESSON_NO 1-14`),
  Luồng 1/2/3 + bảng so sánh; `ROADMAP.md` §3.1/§3.2/§3.3/§3.4 tương
  tự + bảng migration (thêm V5); `PROJECT_INDEX.md` (dòng V5);
  `README.md` (14 mục / 29 bước). File report lịch sử trong
  `ROADMAP.md` §10 giữ nguyên (append-only).

## E. Lỗi bắt được khi test — đường sắt quên đổi số

Lần chạy live đầu tiên sau khi sửa V5 (chưa đủ): plan Luồng 3 trả về
`lessonNo = 1,2,3,4,5,6,7,8,8,9,11,12,13,14` — **số 8 trùng 2 lần,
thiếu số 10**. Nguyên nhân: lúc viết V5 chỉ đổi 7->8, 9->11, 10->12,
11->14 mà **quên bài 8 cũ (Đường sắt) -> 10**. Hậu quả thật: ở Luồng
1/2, `announceItem()` dùng `LESSON_AUDIO[lessonNo]` nên bài đường sắt
sẽ đọc nhầm file lesson-07.mp3 (ghép dọc). Luồng 3 không bị vì loa
đi theo `AUDIO_MAP` (lời loa). Đã sửa V5 thêm UPDATE 8->10 (có WHERE
kèm NAME) + sửa 3 dòng mô tả V5 trong docs, chạy lại live: đúng
1..14. Bài học: đổi số bài phải rà đủ 1 lượt old->new, không chỉ
những bài "bị chen vào".

## F. Verify (sandbox H2, port 18080)

- `node --check exam.js`: OK.
- `javac -parameters` toàn bộ `src/main/java`: OK (exit 0).
- `ExamEngineTest`: **8/8 PASS** (gồm test số tự động bỏ bài 11).
- Live H2: Flyway apply đủ 5 migration (V1–V5), 0 lỗi; login
  `admin/admin123` vào dashboard bình thường.
- Plan API (preset 1):
  - Luồng 3 số sàn: **14 mục**, lessonNo `1..14`, tên đúng thứ tự
    chính thức; Luồng 3 số tự động: **13 mục**, không có bài 11.
  - Luồng 2 số sàn: **29 bước**, lessonNo
    `1,1,1,2,3,3,4,4,4,5,5,6,6,7,7,8,8,8,9,9,10,11,11,12,12,12,13,13,14`.
  - Lỗi ngã tư: đủ 4 lỗi x 4 lần qua (5, 7, 9, 13); tổng **39 lỗi**.
- Trình duyệt thật chạy đủ Luồng 3: 14 mục hiển thị đúng tên/số
  ("Bài 7: Qua ngã tư (2)" ... "Bài 14: Kết thúc"), 14 x + Đúng ->
  **ĐẠT 100/100**, 0 page error (chỉ còn 401 `menu` có từ S09).
- Audio: 11 file lesson duy nhất + beep + result-pass (lesson-05.mp3
  chỉ request 1 lần do trình duyệt cache, các lần qua ngã tư sau
  phát lại từ cache — giống hành vi đã thấy ở S10/S11).

### Bẫy sandbox gặp trong session (ghi để khỏi lặp lại)

1. **Classpath phải là đúng closure của pom.** Lần đầu build
   classpath bằng "mọi jar trong `~/.m2`" (217 jar) đã kéo cả
   `spring-security-web/config` của DriveMaster vào -> Spring Boot
   tự bật filter chain mặc định, trang `/login` thành form "Sign in"
   của Spring thay vì template của app. Sửa: chạy lại
   `fetch_sahinh.py` lấy đúng 92 artifact của pom (chỉ có
   `spring-security-crypto`, không có security-web).
2. **`pkill -f <pattern>` giết luôn shell của chính mình** khi pattern
   xuất hiện trong dòng lệnh exec (đã xảy ra 2 lần trước đây, lần này
   lặp lại). Dừng app bằng `kill <PID chính xác>` lấy từ log
   ("Started ... with PID" / "Tomcat started").

## G. Còn phải kiểm chứng trên máy dev (MySQL thật + V5 thật)

- `git pull`, `docker compose up --build` (V5 migrate trên nền V1–V4
  đã có; DB dev đã có dữ liệu V1–V4 nên V5 chạy UPDATE/INSERT trên
  dữ liệu thật — kiểm tra `flyway_schema_history` lên v5).
- Màn hình Cấu hình bài (SHA0_0100) hiện đủ 14 mục theo đúng thứ tự;
  màn hình Cấu hình lỗi hiện 39 lỗi, trong đó ngã tư có đủ 4 lỗi/lần.
- Chạy hết 3 luồng: Luồng 2 đủ 29 bước; Luồng 3 đủ 14 mục; số tự động
  bỏ bài 11 (Thay đổi số); tình huống nguy hiểm; quá 18 phút trừ điểm
  Luồng 2/3.
- Nghe thử Luồng 2 ở ranh giới bài 10 (đường sắt) — phải đọc
  lesson-08.mp3, không phải lesson-07.mp3 (lỗi đã bắt ở §E).
- Preset đã copy trước V5 (nếu có) vẫn 11 bài — copy lại từ mẫu nếu
  muốn 14 mục.

## H. Ghi chú — khoảng trống có sẵn, KHÔNG sửa trong S12

Bảng lỗi seed V2: các lỗi dừng vạch (không dừng / dừng xa quá 500mm /
dừng quá vạch) chỉ gắn `LESSON_NO=2`, dù PROMPT/ROADMAP ghi "Bài 2, 8"
(nay là 2, 10). Nghĩa là ở bài đường sắt, thầy bấm `-` chỉ thấy lỗi
chung + không thấy 3 lỗi dừng vạch. Đây là thiếu sót từ V2, ngoài
phạm vi S12 (user chỉ báo thiếu bước) — ghi lại để xử lý riêng nếu
user muốn.

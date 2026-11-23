# Session 10 — 2026-11-23 — Âm thanh thi giống loa sân sát hạch (mp3 thu sẵn)

## A. Yêu cầu

User (21:13): "âm thanh bạn lấy được giống y hệt trong bài thi sa hình chính thức không".
Trả lời thật: KHÔNG giống y hệt — app dùng giọng máy SpeechSynthesis của
trình duyệt (mỗi máy một giọng, nghe rô-bốt) + bíp 880 Hz tự tạo bằng code,
trong khi thi thật là âm thanh thu sẵn phát qua loa sân.
User (21:15): "thử cách 2" — tìm video thi thật trên YouTube, tách tiếng loa
ra làm file mp3 gắn vào app.

## B. Kết quả tìm video: KHÔNG có tiếng loa sạch trên YouTube

- Tải audio + phụ đề tự động của 2 video full 11 bài:
  `5a4lmvrjJgg` (CKNN, 845 s) và `w0xf9rJC0l8` (1039 s). 2 video chip cũ
  (`5c_wxSZIeBY`, `SlVibUPw4u4`) đều đã không còn xem được.
- Cài `whisper-small` (faster-whisper 1.2.1, model ~484 MB tải từ
  HuggingFace bằng curl — `huggingface_hub` bị kẹt ở bước xác nhận nên
  không dùng) và nhận dạng toàn bộ 2 video (31 phút, ~2x realtime).
- Kết luận: cả 2 video đều là **thầy giảng suốt từ đầu đến cuối** — mọi câu
  "Bài thi ..." trong bản ghi đều là giọng thầy nam đang giảng, không phải
  loa thi. Không cắt được tiếng loa chính thức từ nguồn này.
- Phân tích phổ đoạn 30–50 s của `w0xf9rJC0l8` (nơi thầy nhắc "tiếng toong
  là báo hiệu"): không có nốt bíp thuần nào nổi lên trên nền giọng nói.

## C. Phương án thay thế đã làm: giọng nữ thu sẵn (TTS) + bíp "tính tong"

Vì không lấy được file gốc của hệ thống chấm thi, dùng giọng nữ đọc thu sẵn
(giọng Aria, tiếng Việt) cho đúng 11 câu thông báo + tình huống nguy hiểm.
Nghe như loa thu sẵn, không còn giọng máy của trình duyệt. **Không phải
giọng loa thi thật** — nếu user ghi được tiếng loa thật ở sân thi/xe chip
thì chỉ cần thay file cùng tên là xong, không sửa code.

- File: `src/main/resources/static/audio/` — `lesson-01.mp3` … `lesson-11.mp3`
  (đúng 11 câu `ANNOUNCE_TEXT` trong V2), `dangerous.mp3`
  ("Dừng xe nguy hiểm" — đúng câu loa thi thật nói), `beep.mp3` (chuông 2 nốt
  1175 Hz → 880 Hz, kiểu "tính tong").
- Kiểm chứng nội dung file: nối 12 file lại, nhận dạng bằng whisper — đọc
  lại đủ 12 câu đúng thứ tự (sai lệch chỉ do whisper nghe nhầm: phát→pha,
  dốc→giốc, vệt→vịt, ghép→kép, sắt→sắc, tín→tính).
- `exam.js`: thêm `AUDIO_MAP` (câu thông báo chuẩn → file mp3). `speak()`
  phát mp3 trước; file lỗi/mất mạng → về giọng máy cũ. Câu không có trong
  map (bước chi tiết như "Thắt dây an toàn", câu tùy chỉnh) vẫn dùng giọng
  máy — thi thật cũng chỉ đọc tên bài, không đọc bước nhỏ. `beep()` phát
  `beep.mp3`, lỗi thì về dao động 880 Hz cũ. `toggleMute` dừng cả mp3.
- `PermissionFilter.java`: thêm `/audio/` vào `isPublic` (như `/css/`,
  `/js/`, `/images/`) — lúc đầu chưa thêm nên `/audio/*.mp3` bị 302 về
  `/login` khi chưa đăng nhập; sau khi thêm trả 200 không cần đăng nhập.

## D. Kiểm thử (sandbox, H2 — xem report_session_09 §B)

- `node --check exam.js` OK; `javac -parameters` 39 file OK;
  `ExamEngineTest` 8/8 PASS.
- Chạy live app trên H2 (cổng 18080): Flyway V1–V4 sạch. Curl: cả 13 file
  `/audio/*.mp3` đều HTTP 200, đúng dung lượng.
- Trình duyệt thật (puppeteer + chrome-headless-shell): login admin vào
  Dashboard; Luồng 3 Bắt đầu → loa đọc "Bài thi xuất phát", điểm 100,
  1/11; bấm + Đúng 2 lần → qua Bài 2, Bài 3. Yêu cầu mạng ghi nhận:
  `lesson-01.mp3` + `beep.mp3` lúc bắt đầu, `lesson-02.mp3`,
  `lesson-03.mp3` ở 2 bài sau (beep các lần sau lấy từ cache trình duyệt
  nên không hiện yêu cầu mới — bình thường). 0 page error, 0 dialog.
- Lỗi console 401 duy nhất: `GET /api/MS/A0/MSA0_0200/menu` — CÓ TỪ TRƯỚC
  (S09), không do S10: trang login gọi `loadMenu()` khi chưa có session
  nên Filter trả 401; sau khi đăng nhập menu trả 200 bình thường (đã kiểm
  chứng bằng curl kèm cookie). Không sửa trong S10 để giữ phạm vi gọn.

## E. Còn lại (máy dev)

## F. Bổ sung sau kiểm chứng câu loa (21:44, trước khi push)

- Tìm thấy file DRAFT (`report_audio_youtube_DRAFT.md`, do subagent viết
  trước đó, chưa commit) ghi mẫu câu loa chính thức là **"Bài thi tiếp
  theo, bài thi <tên bài>"**. Kiểm chứng lại bằng 2 nguồn độc lập:
  thienminh-autosafety.com trích nguyên văn "Bạn sẽ nghe thấy lệnh 'Bài thi
  tiếp theo, bài thi kết thúc' từ hệ thống loa trên xe chip"; botuctaylai.edu.vn
  ghi "khi nghe hiệu lệnh 'bài thi tiếp theo'". **Kết luận: tiền tố "Bài thi
  tiếp theo" là thật** — file mp3 ban đầu thiếu nó.
- Đã tạo lại `lesson-02.mp3` … `lesson-11.mp3` với tiền tố ("Bài thi tiếp
  theo, bài thi …"); bài 1 giữ "Bài thi xuất phát" (chưa có "tiếp theo" ở
  bài đầu; lúc xuất phát loa gọi "xe số … bắt đầu thi" nhưng app không biết
  số xe nên giữ tên bài). Chữ hiện trên màn hình vẫn là tên bài, chỉ tiếng
  đọc có thêm tiền tố.
- Thêm `result-pass.mp3` ("Chúc mừng bạn đã thi đạt phần thi thực hành lái
  xe trong hình", theo thienminh: qua vạch kết thúc nghe "Tút" hoặc "Chúc
  mừng bạn đã thi đạt"): `finish()` phát khi ĐẠT, tôn trọng nút tắt loa.
- Kiểm thử lại toàn bộ Luồng 3 (11 lần + Đúng): kết quả ĐẠT 100/100, yêu cầu
  mạng ghi đủ lesson-01…lesson-11 + beep + result-pass.mp3 theo đúng thứ tự.
  0 page error (vẫn chỉ 401 menu ở trang login như mục D).
- File DRAFT đã xóa sau khi gộp nội dung đã kiểm chứng vào report này; nhận
  định sai trong DRAFT ("yt-dlp bị 403, không tải được trong sandbox") không
  đúng — tải + nhận dạng 2 video đã chạy được ở §B.

## G. Còn lại (máy dev)

## H. S10b — sửa sau khi user báo "pull về vẫn y như cũ" (23:25)

- **Nguyên nhân thật:** 2 khả năng đều cho đúng triệu chứng này — (1) user
  chạy `docker compose up` mà quên `--build` nên JAR cũ vẫn chạy (file mp3
  không có trong JAR cũ, `playFile` lỗi -> lặng lẽ về giọng máy); (2) S10
  chỉ phát mp3 cho câu trong `AUDIO_MAP` — Luồng 1/2 toàn bước chi tiết
  ("Thắt dây an toàn", "Qua vệt bánh xe"...) không có trong map nên toàn
  bộ vẫn giọng máy, chỉ Luồng 3 nghe được mp3 mới. Dockerfile đã đúng
  (`COPY src` + `mvn package`, không có .dockerignore loại mp3) nên chỉ
  cần rebuild là file vào JAR.
- **Sửa (exam.js, `announceItem()`):** bước chi tiết vừa sang bài mới thì
  phát mp3 của bài đó (map `LESSON_AUDIO` theo số bài, khớp V2); các bước
  sau trong cùng bài giữ giọng máy đọc tên bước như cũ. Luồng 3 không đổi.
- **Test Luồng 2 (23 bước):** Bắt đầu (Bài 1 bước 1) -> lesson-01.mp3;
  bước 2-3 Bài 1 không phát thêm; bước 1 Bài 2 -> lesson-02.mp3; bước 1
  Bài 3 -> lesson-03.mp3, bước 2 không phát thêm; bước 1 Bài 4 ->
  lesson-04.mp3, bước 2-3 không phát thêm. 0 page error (vẫn chỉ 401 menu
  ở trang login như mục D).
- **Hướng dẫn user kiểm tra trên máy dev:** `git pull` rồi
  `docker compose up --build` (bắt buộc có `--build`, không thì JAR cũ);
  mở http://localhost:8080/audio/lesson-02.mp3 trên trình duyệt — nghe được
  là bản mới đã lên; hard-refresh trang thi (Ctrl+Shift+R) nếu trình duyệt
  cache exam.js cũ.

- `git pull` rồi `docker compose up --build`, mở Luồng 3 nghe thử tiếng loa.
  File mp3 nằm trong repo nên máy dev có sẵn, không cần tải thêm.
- Muốn giống y hệt loa thi thật: ghi âm tiếng loa ở sân thi hoặc xe chip
  (11 câu + "Dừng xe nguy hiểm" + tiếng "tính tong"), gửi file cho Muse
  thay vào `static/audio/` cùng tên file — không cần sửa code.
- Lỗi console 401 ở trang login (mục D) có thể dọn sau: cho `loadMenu()`
  bỏ qua khi chưa đăng nhập.

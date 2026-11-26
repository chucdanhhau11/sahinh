# Report Session 13 — 2026-11-26 — Thay 11 file loa bằng giọng mới (user gửi)

## A. User yêu cầu

- 16:13: user gửi voice note 90,3s (file `16.13_26-11-2026_1_gotd.m4a`)
  đọc đủ 14 câu loa theo trình tự thi, bảo: "Cắt chính xác âm thanh này
  theo các bước, thiếu ở đâu bỏ qua và báo cáo cho tôi, nhớ là cắt chính xác".
- Sau khi cắt xong + kiểm tra lại, user chốt (22:01): thay hết 11 file
  bằng giọng mới.

## B. Nguồn thu — giọng nữ khác loa TikTok S11

- Nhận dạng full-file bằng whisper-small (word timestamps) + đo F0:
  giọng nữ, F0 trung vị 271–286 Hz. Loa TikTok S11 là 193–213 Hz; user
  là nam — file này KHÔNG phải giọng user, KHÔNG phải loa TikTok.
- Trong file có jingle quảng cáo "Hãy subscribe cho kênh Ghiền Mì Gõ…"
  (~58–65s, đúng jingle đã thấy trong video TikTok S11) → thu này lấy
  từ video, không phải thu trực tiếp tại sân.
- Cấu trúc file: 14 câu loa đọc sạch theo đúng thứ tự (Bắt đầu +
  12 câu "Bài thi tiếp theo…" + Kết thúc), chen jingle giữa bài 11 và
  12, cuối file thừa 1 tiếng "Bắt đầu" (như bắt đầu lượt đọc thứ 2).

## C. Cắt (chi tiết: cut-user/CUT_LOG.md)

- Định vị từng câu bằng word timestamps; cắt bằng ffmpeg với `-ss`/`-to`
  TRƯỚC `-i` (bài học S11: afade + `-ss` sau `-i` = file câm),
  fade in 0,03s / out 0,05s, mp3 128k, biên 0,15s quanh từ đầu/cuối.
- Đủ 14 mục, KHÔNG thiếu mục nào — "Kết thúc" @80,50–83,18 lúc đầu
  đọc full-file bị nhòe, đọc lại theo cửa sổ 80,5–90,3s mới rõ.
- Loại bỏ: jingle Ghiền Mì Gõ, "Bắt đầu" thừa @88,7s, 2 tiếng lạ ngắn
  @66–67s và @84,5–85,5s.
- 11 file lesson-01…11 + 3 bản ngã tư dự phòng (ngatu-2/3/4; app chỉ
  cần 1 file lesson-05 chung cho cả 4 lần qua).
- Âm lượng chuẩn hoá −5 dB (lesson-01 +2 dB bù thêm) về −19,2…−21,3 dB,
  khớp file app hiện tại (−18,3…−21,5 dB).
- Đọc lại từng file cắt bằng whisper-small: 13/14 rõ đúng câu;
  lesson-11 đọc lại theo cửa sổ có ngữ cảnh mới rõ ("…kết thúc").

## D. Kiểm tra lại theo yêu cầu user — whisper-medium đối chiếu từng từ

- Tải faster-whisper-medium (1,53 GB) để có ý kiến độc lập thứ hai;
  so từng từ với text chuẩn trong app (AUDIO_MAP = ANNOUNCE_TEXT seed
  V2 + tiền tố "Bài thi tiếp theo, " ở file 02–11 theo quy ước S10).
  Kết quả: `cut-user/recheck-medium.json`.
- Khớp tuyệt đối (1,00): lesson-02, lesson-06, lesson-11.
- Khớp, chỉ sai do máy nghe nhầm âm quen thuộc (small đã nghe đúng):
  lesson-03 (dừng→rừng), lesson-04 (hẹp→hạt), lesson-07 (ghép→ghét),
  lesson-10 (ghép→gắt, đỗ→đổ).
- LỆCH TỪ THẬT so với text trong app (người đọc nói khác):
  * lesson-01: nói "Bắt đầu" (text "Bài thi xuất phát"; file S11
    "Bắt đầu thi"). Lưu ý: medium BỊA RA câu "Hãy đăng ký kênh…"
    trên clip 1 giây này (ảo giác whisper với clip quá ngắn); kết luận
    "Bắt đầu" dựa vào small + word timestamps.
  * lesson-05 (+3 bản dự phòng): nói "tín hiệu điều khiển giao thông"
    (text "đèn tín hiệu").
  * lesson-08: nói "dừng xe chỗ có đường sắt chạy qua"
    (text "tạm dừng ở chỗ có đường sắt chạy qua").
  * lesson-09: cả 3 lần đọc độc lập (small full-file, small clip rời,
    medium clip rời) đều ra "đường thẳng" thay vì "đường bằng". Kiểm tra
    âm đầu bằng phổ (so với "Bắt" [b] và "thay" [th] cùng file) không
    kết luận được chắc chắn — khả năng cao người đọc nói "thẳng", nhưng
    không loại trừ máy nghe nhầm. User đã biết và vẫn chốt thay hết.
- App map file theo tên (LESSON_AUDIO/AUDIO_MAP) nên các lệch từ trên
  KHÔNG làm hỏng phát tiếng — file vẫn phát đúng bài.

## E. Thay file + verify (sandbox H2, port 18080)

- Thay 11 file lesson-01…11 trong `static/audio/` bằng bản cắt mới.
  Giữ nguyên: beep.mp3 (chuông thật S11), dangerous.mp3 (TTS),
  result-pass.mp3 (TTS). File loa TikTok S11 vẫn còn trong lịch sử git
  (commit ed58183) + bản cắt gốc ở `sahinh-audio-work/cut-real/`.
- `node --check exam.js`: không đổi code nên không chạy lại (không sửa
  code trong S13).
- `javac -parameters` toàn bộ `src/main/java` + test: OK.
- `ExamEngineTest`: **8/8 PASS**.
- Live H2: login 200; curl 11 file `/audio/lesson-*.mp3` đều 200 và
  md5 KHỚP đúng bản cắt mới; beep/dangerous/result-pass vẫn 200.
- Trình duyệt thật chạy đủ Luồng 3 (bản test đầu bấm thừa nút "Qua bài"
  nên nhảy cóc 2 bài/lần — lỗi test, không phải lỗi app; đã sửa test chỉ
  bấm "+ Đúng"): 14 mục đúng thứ tự chính thức → **ĐẠT 100/100**,
  audio request lesson-01…11 + beep + result-pass theo thứ tự, chỉ còn
  lỗi 401 `menu` có từ S09.
- /tmp đã bị xoá từ sau S12 nên phải dựng lại infra test: fetch_sahinh.py
  (92 jar), tải lại h2-2.2.224.jar, copy repo sang /tmp/sahinh-h2, thay
  V4 bằng bản H2 (`ADD COLUMN IF NOT EXISTS`) — chỉ là scratch, không
  commit.

## F. Còn phải kiểm chứng trên máy dev (MySQL thật)

- `git pull`, `docker compose up --build` (chỉ thay file mp3, không có
  migration mới), nghe thử đủ 3 luồng bằng giọng mới — đặc biệt
  lesson-01 ("Bắt đầu"), lesson-05 ("tín hiệu điều khiển giao thông"),
  lesson-08 ("dừng xe chỗ có…"), lesson-09 ("thẳng" hay "bằng").
- Nếu user nghe lesson-09 là "thẳng" và muốn đúng text "đường bằng":
  thu lại riêng câu đó rồi thay 1 file lesson-09.mp3 là xong, không
  sửa code.

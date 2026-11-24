# S11 — Âm thanh loa thi THẬT tách từ video TikTok (23–24/11/2026)

## A. Bối cảnh

- S10 dùng giọng nữ TTS Aria (không phải loa thật). User 24/11 08:17 giao
  kiểm tra video TikTok quay trong xe (lead từ bản kiểm tra audio 23/11):
  https://www.tiktok.com/@chidinh_daylaixe/video/7532639865452776711
  (full 11 bài hạng B, sân Củ Chi, bạn nữ đậu 100 — thực tế 98 điểm).
- User 24/11 21:12 xác nhận "âm thanh trong video tiktok oke, tách ra giúp tôi".

## B. Tải + nhận dạng

- yt-dlp 2026.11.23 tải được audio 16,85 MB, 1104,2 s (18:24). Lưu ý môi
  trường: sau khi VM thay mới, `~/.local` site-packages không còn trên
  sys.path của root (`ENABLE_USER_SITE: False`) — phải
  `export PYTHONPATH=/home/hatch/.local/lib/python3.12/site-packages`
  thì mới import được yt_dlp/faster_whisper; cài lại 2 gói bằng pip
  `--user --break-system-packages`. faster-whisper 1.2.1 + PyAV hiện tại
  lỗi `av.open() ... metadata_errors` — helper
  `/opt/muse/assets/audio/transcribe_chunks.py` tránh được bằng cách decode
  ffmpeg ra PCM thô rồi đưa ndarray vào `model.transcribe()`; script tự
  viết cũng phải làm vậy.
- Nhận dạng toàn bộ bằng whisper-small (19 chunk 60 s, ~9 phút): video có
  giọng loa nữ THẬT xen với giọng thầy nam hướng dẫn liên tục trong xe.
  Xác minh loa là giọng nữ bằng F0 trung vị: loa 193–213 Hz, thầy nam
  125–126 Hz ("Bắt đầu thi" @13,4 s đo 213 Hz → đúng loa, không phải thầy).

## C. Câu loa tìm được (mốc thời gian = giây trong video)

| File | Câu loa thật | Mốc cắt | Ghi chú |
|---|---|---|---|
| lesson-01.mp3 | "Bắt đầu thi" | 13.15–14.55 | Tín hiệu xuất phát, F0 213 Hz. Không có phần "xe số …" |
| lesson-02.mp3 | "Bài thi tiếp theo, bài thi dừng xe nhường đường cho người đi bộ" | 22.75–26.35 | |
| lesson-03.mp3 | "Bài thi tiếp theo, bài thi dừng xe và khởi hành ngang dốc" | 58.85–62.50 | |
| lesson-04.mp3 | "Bài thi tiếp theo, bài thi qua vệt bánh xe và đường hẹp vuông góc" | 90.70–94.40 | Lần chạy đầu whisper nghe nhầm thành promo "subscribe" — chạy lại có VAD mới ra đúng câu |
| lesson-05.mp3 | "Bài thi tiếp theo, bài thi qua ngã tư có đèn tín hiệu" | 638.00–642.00 | Lấy lần đọc thứ 2 (@638 s, rõ hơn lần @350 s). Video đọc bài 5 hai lần — video có thể ghép từ 2 lượt chạy |
| lesson-07.mp3 | "Bài thi tiếp theo, bài thi ghép xe dọc vào nơi đỗ" | 412.20–415.30 | |
| lesson-09.mp3 | "Bài thi tiếp theo, bài thi thay đổi số trên đường bằng" | 763.40–767.20 | |
| lesson-10.mp3 | "Bài thi tiếp theo, bài thi ghép xe ngang vào nơi…" | 801.50–804.45 | **Cắt ngắn, thiếu chữ "đỗ"** — thầy bắt đầu nói "Chỗ khẩn cấp thứ 3…" từ 804.4 s đè lên chữ "đỗ" (F0 từ 804.6 s toàn giọng thầy 92–144 Hz). Hai lần nhận dạng cửa sổ dài đều ra đúng câu loa, nhưng cắt file riêng ra whisper nghe không ổn định (giọng thầy xen) — file vẫn giữ vì loa nghe được 802–804 s, đã ghi chú để user tự nghe đánh giá |
| lesson-11.mp3 | "Bài thi tiếp theo, bài thi kết thúc" | 1085.60–1088.20 | |
| beep.mp3 | Chuông "tính tong" 2 nốt | 20.30–21.50 | Đo phổ: nốt 1 ≈ 875 Hz (20.4–20.7 s), nốt 2 ≈ 734 Hz (20.8–21.4 s). Chuông tổng hợp S10 (1175→880 Hz) là SAI cao độ — đã thay bằng bản thu thật |
| — | Tình huống nguy hiểm | — | **KHÔNG tách được câu "Dừng xe nguy hiểm"**: còi báo vang @714.5 s (phổ thấy luân phiên 547/656/938 Hz), thầy nói "Dừng, ấn cái nút đó" @715.1 s đè lên; whisper 2 lần chạy không VAD đều không tìm thấy câu loa, chỉ lần gợi ý prompt mới "nghe ra" (không tin được). Giữ file TTS cũ |
| — | Chúc mừng thi đạt | — | **KHÔNG có trong video**: cuối video (1100–1104 s) chỉ còn jingle quảng cáo "subscribe kênh La La School" (lần đọc đầu nghe nhầm thành "Bạn đã thi trạm"). Giữ file TTS cũ |

**Không có trong video, giữ nguyên giọng TTS Aria:** lesson-06 (đường vòng
quanh co — thầy chỉ nhắc tên bài @283.7 s, không có câu loa), lesson-08
(tạm dừng ở chỗ có đường sắt — đã quét 652–700 s cả VAD lẫn không VAD,
không có câu loa), dangerous.mp3, result-pass.mp3. Vậy 10/14 file là loa
thật, 4/14 vẫn TTS.

## D. Cách cắt + kiểm chứng

- Cắt bằng ffmpeg, re-encode mp3 128k, fade in/out 0.03/0.05 s, tăng âm
  lượng +3 dB cho 9 file câu loa (mức trung bình từ ≈ −23 dB lên ≈ −20 dB,
  gần bằng file TTS −18.9 dB; beep giữ nguyên −17.2 dB vì chuông vốn to).
- **Bẫy đã gặp:** lần cắt đầu toàn file câm — `afade=t=out` khi `-ss` đặt
  SAU `-i` sẽ áp theo timestamp gốc của file (22.75 s…) nên fade-out ở
  3.75 s rơi vào trước khi clip bắt đầu → câm hoàn toàn. Sửa: đặt `-ss`
  và `-to` TRƯỚC `-i` (input option) thì timestamp về 0, fade chạy đúng.
- Kiểm chứng từng file cắt bằng cách nhận dạng lại bằng whisper: 9/10
  đọc đúng câu loa (sai chính tả do whisper nghe nhầm dấu — vd "rừng" =
  "dừng", "VNT" = "vệt bánh xe" — nhưng tiếng trong file là đúng);
  lesson-10 như mục C; beep kiểm chứng bằng phổ (875→734 Hz), whisper
  nghe nhầm thành "Cảm ơn các bạn" là bình thường vì đó là chuông, không
  phải giọng nói.
- Video còn lẫn 2 jingle quảng cáo chèn ("subscribe kênh Ghiền Mì Gõ"
  @~90 s và @~253 s, "La La School" @1100 s) — không dính vào file nào đã cắt.

## E. Test live (sandbox H2, như S09/S10)

- 13/14 file mp3 đều HTTP 200 (lesson-06 TTS 26917 B vẫn nguyên).
- Trình duyệt thật chạy đủ Luồng 3 (11 lần + Đúng): ĐẠT 100/100, yêu cầu
  mạng ghi đủ lesson-01…11 + beep + result-pass theo thứ tự. 0 page error
  (vẫn chỉ 401 menu ở trang login như S09/S10).
- Luồng 2 không cần test lại (S10b đã xong, S11 chỉ đổi nội dung file mp3).

## F. Còn lại (máy dev)

- `git pull` rồi `docker compose up --build` (bắt buộc có `--build`).
  Nghe thử Luồng 3: bài 1 "Bắt đầu thi", các bài sau "Bài thi tiếp theo,
  bài thi …", chuông "tính tong" thật ở mỗi lần qua bài.
- Bài 6, bài 8, câu nguy hiểm, câu chúc mừng vẫn là giọng TTS — nếu user
  tìm được video khác có 4 câu này thì tách nốt thay vào cùng tên file.
- Riêng lesson-10: nhờ user nghe đánh giá (câu bị cắt ngắn ở "…vào nơi",
  thiếu "đỗ"); nếu thấy khó nghe thì quay lại file TTS cũ cho bài 10.

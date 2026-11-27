# Report Session 14 — 2026-11-27 — Sửa tiếng chồng chéo (loa cũ + loa mới)

## A. User báo lỗi (27/11 11:57)

"âm thanh lỗi tùm lum hết vậy bạn, chồng chéo cả âm thanh cũ" — sau khi
pull S13, nghe thi bị chồng tiếng: tiếng bài trước vẫn phát trong khi
tiếng bài sau đã đọc. (File mp3 S13 không sai — lỗi nằm ở code phát
tiếng trong `exam.js`, có từ S10.)

## B. Nguyên nhân — 2 lỗi trong `exam.js` (bắt bằng probe, không đoán)

Đọc code + chạy probe với `window.Audio` được bọc lại để ghi từng sự
kiện play/pause/playing/ended (file probe: `/tmp/s14-probe.js`,
`/tmp/s14-probe-b.js` — /tmp, không commit):

1. **File cũ thành "mồ côi", không bao giờ bị dừng.** `playFile()` cũ
   chỉ ghi đè biến `currentAudio = a` mà KHÔNG pause file đang phát.
   `show()` gọi `announceItem()` (loa đọc bài) rồi `beep()` ngay sau
   đó — beep ghi đè `currentAudio`, tiếng loa bài đó mất tham chiếu,
   `stopAudio()` về sau chỉ dừng được beep (đã phát xong) chứ không
   dừng được tiếng loa. Thầy bấm nhanh (Luồng 2/3) là tiếng bài trước
   chồng lên tiếng bài sau.
2. **Handler catch xoá nhầm tham chiếu.** `catch`/`onerror` cũ xoá
   `currentAudio` vô điều kiện. Probe thấy: beep bị pause ngay sau
   `play()` nên promise reject `AbortError`; catch chạy MUỘN, xoá luôn
   tham chiếu tới file loa đang phát dở → file đó cũng thành mồ côi.
   Cùng 1 lỗi, 2 đường rò.
3. **Hệ quả phụ (phát hiện thêm):** vì beep bị pause ngay trong cùng
   1 tác vụ sau khi play, chuông "tính tong" thật KHÔNG BAO GIỜ kêu
   được — chỉ còn beep oscillator 880 Hz dự phòng (tiếng "cũ" user
   nghe thấy có thể là cái này).

## C. Cách sửa (chỉ `static/js/exam.js`, không đổi file mp3)

- `playFile(file, fallback, onEnded)`: luôn pause file đang phát
  trước khi mở file mới; handler `onended`/`onerror`/`catch` chỉ xoá
  `currentAudio` khi nó vẫn trỏ đúng element đó; `AbortError` (bị dừng
  do chuyển bài nhanh) thì không fallback, không chạm `currentAudio`.
- `playBeepThen(fn)` mới: beep "tính tong" phát TRỌN rồi mới đọc loa
  (như thi thật). `fn` chỉ chạy khi beep xong VÀ chưa sang bài khác
  (`showSeq` đổi mỗi lần `show()`). Có timeout dự phòng 900ms vì sự
  kiện `ended` của Audio có thể tới muộn hơn cú bấm tiếp theo (đã gặp
  ở test Luồng 2); bên nào tới trước thì đọc, bên kia no-op.
- `show()`: dừng tiếng cũ, kiểm tra `busy` — nếu tiếng trước vẫn đang
  phát thì BỎ QUA beep, đọc thẳng tên bài (nếu không, thầy bấm theo
  nhịp beep sẽ không bao giờ nghe được tên bài). Beep chỉ phát khi
  đang yên lặng. Đồng hồ bước vẫn chạy từ lúc vào bước (như cũ).
- `triggerHazard()`: beep rồi mới đọc "Tình huống nguy hiểm"; tiếng
  bài hiện tại nhường cho cảnh báo.
- `finish()`: dừng tiếng bài cuối trước khi phát câu chúc mừng.
- Xoá hàm `beep()` cũ (không còn ai gọi).

## D. Verify (sandbox H2, port 18080)

- `node --check exam.js`: OK. `javac -parameters` + `ExamEngineTest`:
  **8/8 PASS** (không đổi code Java, chạy lại cho chắc).
- Test chồng tiếng (`/tmp/s14-test.js`): bọc `window.Audio`, ghi mọi
  lần play/pause, kiểm tra không có 2 khoảng phát nào chồng nhau:
  * A — Luồng 3 bấm nhanh 300ms/bài: **0 chồng**, ĐẠT 100/100, đủ
    lesson-02…11 + beep + result-pass theo thứ tự. (lesson-01 không
    phát vì thầy chấm bài 1 chỉ sau 300ms — đúng thiết kế: bài đã chấm
    thì không đọc lại.)
  * B — Luồng 2 bấm 1500ms/bước qua ranh giới bài: **0 chồng**,
    lesson-02/03/04 đủ. Probe riêng (`s14-probe-b.js`, chỉ bấm Bắt
    đầu rồi đợi) xác nhận bước đầu Luồng 2 phát đúng: beep trọn
    0,83s → lesson-01 phát trọn 1,06s → ended, 0 page error.
  * C — Luồng 1 tự động 45s qua ranh giới bài 1→2: **0 chồng**.
- Lỗi test gặp giữa chừng (đã sửa, không phải lỗi app): lần test đầu
  dùng `beep()` trước `announceItem()` trong cùng tác vụ làm beep bị
  pause ngay (không nghe được chuông); lần test sau phát hiện bấm
  theo nhịp beep thì tên bài bị bỏ qua → thêm kiểm tra `busy` và
  timeout 900ms.
- Lưu ý test: phải kill đúng PID của JVM (số trong log "INFO <pid>"),
  kill PID của wrapper exec không tắt được app (gặp 3 lần trong S14).

## E. Còn phải kiểm chứng trên máy dev (MySQL thật)

- `git pull`, `docker compose up --build`, hard-refresh (Ctrl+Shift+R)
  để chắc trình duyệt lấy `exam.js` mới (file mp3 giữ nguyên tên nên
  trình duyệt có thể cache — nếu vẫn nghe tiếng cũ thì xoá cache hoặc
  mở tab ẩn danh kiểm tra).
- Nghe thử đủ 3 luồng, bấm nhanh Luồng 2/3: chỉ 1 tiếng tại 1 thời
  điểm, chuông "tính tong" kêu trọn trước mỗi tên bài.
- Hỏi lại user: lỗi chồng tiếng gặp ở luồng nào (để đối chiếu), và
  "âm thanh cũ" họ nghe là giọng máy robot hay giọng loa TikTok S11.

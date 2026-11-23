# S08 — 2026-11-23 — Fix vòng lặp tải lại trang login

## A. Triệu chứng (user báo, có ảnh chụp)

App chạy bằng `docker compose up --build` lên được, trang login hiện form
đầy đủ (admin/admin123 đã điền sẵn), nhưng trang cứ load liên tục, không
nhập hay bấm gì được.

## B. Nguyên nhân (đọc code, không đoán)

Vòng lặp tải lại vô hạn, 2 chỗ cùng gây ra:

1. `src/main/resources/static/js/app.js` — `api()` cứ thấy HTTP 401 là
   `window.location.href = '/login'`. Trang login chạy `loadSession()` +
   `loadMenu()` ngay khi mở trang (`DOMContentLoaded`).
2. `PermissionFilter.isPublic()` KHÔNG có `/api/ZZ/A0/ZZA0_0001/session`
   (chỉ có `/login` của API login) nên request kiểm tra phiên khi chưa đăng
   nhập bị Filter chặn bằng HTTP 401, chưa tới được trx.

Chuỗi lặp: mở `/login` -> `loadSession()` gọi `/session` -> Filter trả 401
-> `api()` gán `location.href = '/login'` -> trang tải lại -> lại gọi
`/session` -> lại 401 -> ... Trang tải lại liên tục nên user không kịp
nhập hay bấm gì. `loadMenu()` gọi `/api/MS/A0/MSA0_0200/menu` cũng 401
nên cũng góp 1 lần chuyển hướng mỗi vòng lặp.

Trx `session()` vốn đã trả JSON `{code:401}` với HTTP 200 (xem
`ZZA0_0001_trx.java`), tức thiết kế gốc đã tính cho `loadSession()` xử lý
êm — chỉ bị Filter chặn trước nên không tới được.

## C. Cách sửa (2 file, không đổi flow đăng nhập)

- `PermissionFilter.java`: thêm `/api/ZZ/A0/ZZA0_0001/session` và
  `/api/ZZ/A0/ZZA0_0001/logout` vào `isPublic()`. `/session` giờ qua
  Filter, trx trả JSON `{code:401}` HTTP 200, `loadSession()` trả `null`
  như thiết kế. `/logout` không cần phiên nên cho qua cũng an toàn.
- `app.js` — `api()`: chỉ chuyển về `/login` khi 401 xảy ra ở trang khác
  trang login (`pathname` khác `/` và `/login`). Thêm cờ `noRedirect` và
  dùng ở `loadSession()` / `loadMenu()` (2 lệnh thăm dò, không phải hành
  động cần đăng nhập). Trang đã đăng nhập mà hết phiên vẫn bị đẩy về
  `/login` như cũ (giữ nguyên hành vi bảo vệ các trang thi).

## D. Kiểm thử (sandbox — đã chạy thật 2026-11-23)

- `node --check` `app.js`: **OK**.
- Mô phỏng JS bằng `vm` (stub `window`/`document`/`fetch`, `/tmp/s08_sim.js`,
  không commit): **4/4 PASS**
  - T1 trang `/login` + 401: không redirect (lỗi cũ: redirect -> lặp).
  - T2 trang `/` + 401: không redirect.
  - T3 trang dashboard + 401: vẫn redirect về `/login` (giữ hành vi cũ).
  - T4 trang `/login` + session 200: hiện tên user, không redirect.
- `javac -proc:none` toàn bộ `src/main/java`: **exit 0, 0 lỗi**;
  `javac` test: **exit 0**.
- `ExamEngineTest`: **8/8 PASS, 0 failed** (không đổi engine, chạy lại để
  chắc không hồi quy).

## E. Chưa verify live (cần máy dev — giống S01–S07)

Sandbox không có Docker/MySQL và bước cài MariaDB bằng `apt` đã bị user
từ chối ở lượt này nên không start được app thật trong sandbox. Cần user
chạy lại trên máy dev:

```bash
docker compose up --build
# mở http://localhost:8080 -> trang login đứng yên, nhập admin / admin123
# bấm Đăng nhập -> vào Dashboard, không còn tải lại liên tục
```

Kiểm tra thêm: hết phiên ở trang thi thì vẫn bị đẩy về `/login` (T3).

## F. Cập nhật sau S08 (23/11, user bảo start thử bằng Java)

User hỏi sao lại dùng node trong khi app dùng Java — làm rõ: node CHỈ dùng
để `node --check` + mô phỏng logic file `app.js` (JavaScript trình duyệt),
app vẫn chạy bằng Java (javac + `java -cp ... SaHinhApplication`).

Đã thử start app thật bằng Java trong sandbox sau khi user bấm cho phép
cài MariaDB: MariaDB 10.11 chạy được, `mysql` CLI (cả socket lẫn TCP)
kết nối bình thường, nhưng app Java chết ở Flyway với lỗi
`Packet for query is too large (7,566,701 > 65,535)`. Truy vết tới cùng:
kết nối TCP thô bằng Java tới 127.0.0.1:3306 nhận về chuỗi
`muse: Database connections is turned off for this assistant. To ...`
(trong khi Python nối cùng cổng nhận đúng handshake MariaDB). Tức
**sandbox chặn kết nối DB từ tiến trình Java của assistant** — mọi driver
(MySQL Connector/J 8.3.0, MariaDB Connector/J 3.3.3) đều đọc nhầm 3 byte
đầu `mus` thành độ dài gói 7.566.701. Đây là giới hạn của sandbox, không
phải lỗi code/app: trên máy dev chạy Docker (MySQL 8 thật) app đã lên tới
trang login như ảnh user gửi. Không cài MySQL/MariaDB trong sandbox nữa
để chạy app — chỉ verify bằng javac + test + mô phỏng JS, chạy live để
máy dev làm.

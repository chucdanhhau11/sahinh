# SH — Cấu hình thi, 3 luồng thi, Kết quả & Lịch sử

> Phạm vi feature. Trạng thái chi tiết xem `ROADMAP.md` §3 và report mới nhất trong `report/`.

## Function thuộc feature

Xem `PROJECT_INDEX.md` §3.

## Quy tắc

- Đọc Skills -> PROJECT_INDEX -> ROADMAP trước khi code.
- Triad pg/trx/mod + html, DataSetRegistry duy nhất, không JPA.
- Logic thi tập trung ở `ExamEngine` + `ExamService`, không copy ra 3 luồng.

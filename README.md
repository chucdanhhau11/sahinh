# SaHinh-B2 — App luyện thi sa hình hạng B

App web mô phỏng thi sa hình hạng B (B2 cũ, từ 01/01/2025 gộp thành hạng B).
Không cảm biến, không phần cứng: mô phỏng bằng đồng hồ + loa + thầy bấm tay.

## 3 luồng thi (đã chốt — xem `PROMPT.md` §4)

| Luồng | Cách chạy |
|---|---|
| **1 — Thi chuẩn, tự động** (`/SH/SHA0_0400`) | Thầy chỉ bấm **Bắt đầu 1 lần**, máy tự đếm giờ, tự đọc loa, tự chuyển bước tới Kết thúc. Sai / hết giờ ở bước nào thì **dừng ngay và làm lại từ đầu**. Giây chỉnh tự do theo từng thầy, không ép luật 18 phút. |
| **2 — Bấm chay chi tiết** (`/SH/SHA0_0500`) | Thầy tự bấm qua từng **bước nhỏ** (~23 bước), bấm **+** nếu đúng, **-** nếu sai (chọn lỗi cụ thể, máy tự trừ điểm theo DB). Lỗi liệt -> **RỚT** ngay. |
| **3 — Bấm chay thi thật** (`/SH/SHA0_0600`) | **Không bước nhỏ**, chỉ 11 bài. Loa chỉ đọc tên bài (vd "Bài thi xuất phát" đếm 20 giây). Thầy **phải bấm** mới qua bài, cũng bấm **+ / -**. Không chỉnh giờ/điểm trong luồng này. |

- Bắt đầu 100 điểm, đạt từ 80. Điểm liệt cấu hình được, ví dụ **tắt máy trên dốc = rớt**.
- Preset: Superadmin setup sẵn, thầy bấm **Áp dụng** để dùng, hoặc **Tự setup riêng** (hệ thống copy ra bản riêng, không ghi đè bản gốc).
- Bước nhỏ có nút bật/tắt: tắt thì chỉ đọc tên bài + đếm giờ bài, bật thì đọc từng bước nhỏ.

## Chạy

```bash
docker compose up --build
# mở http://localhost:8080
```

Tài khoản seed (đổi mật khẩu sau lần đăng nhập đầu tiên):
- Superadmin: `admin` / `admin123`
- Giáo viên: `teacher` / `teacher123`
- Học viên: `student` / `student123`

Chạy JAR lẻ (khi MySQL đã lên):

```bash
DB_HOST=localhost DB_PORT=3306 DB_NAME=sahinh_b2 DB_USER=root DB_PASSWORD=root \
  java -jar target/sahinh-b2-0.1.0-SNAPSHOT.jar
```

## Build / test (máy dev)

```bash
mvn test && mvn clean package
curl http://localhost:8080/actuator/health
```

## Kiến trúc (xem `skills/#SAHINH-B2—SKILLS.txt` — luật tối cao)

- Triad mỗi function: `*_pg.java` (controller, chỉ mapping + template)
  -> `*_trx.java` (REST API, validate + gọi mod + JSON `{code,message,data}`)
  -> `*_mod.java` (toàn bộ nghiệp vụ + SQL) + `.html` (Thymeleaf).
- Truy cập DB **chỉ** qua `DataSetRegistry` / `DataSet` (JDBC thuần).
  Cấm JPA / Repository / JdbcTemplate / Hibernate / Entity.
- Logic thi tập trung trong **1 engine duy nhất** `ExamEngine` + `ExamService`,
  dùng chung cho 3 luồng. Không copy logic tính điểm ra 3 nơi.
- Âm thanh: SpeechSynthesis vi-VN + Web Audio beep, không file mp3.
- Quyền chỉ kiểm tra ở Filter (`PermissionFilter`), menu đọc từ DB.

## Tài liệu

- `PROMPT.md` — prompt chi tiết để agent/AI khác build lại app này.
- `ROADMAP.md` — trạng thái project, 11 bài, bảng lỗi, kế hoạch S01–S07.
- `PROJECT_INDEX.md` — bản đồ mã nguồn, module, migration.
- `docs/features/<FEATURE>/report/` — báo cáo từng session.

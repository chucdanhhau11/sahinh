-- V5 — S12: trình tự thi chính thức là 14 mục, không phải 11 bài.
-- User gửi ảnh màn hình CÀI ĐẶT CHỨC NĂNG (26/11): xe qua ngã tư 4 lần
-- trong sân, mỗi lần là 1 mục chấm riêng (ngã tư (1)..(4)); V2 gộp cả 4
-- lần thành 1 bài nên thiếu 3 mục. LESSON_NO trở thành số thứ tự chính
-- thức 1-14 (trùng số hiển thị "Bài N" trên màn hình thi).
-- Chỉ sửa preset mẫu id=1 ("Chuẩn thi thật"); preset đã copy trước V5
-- giữ nguyên bố cục cũ (superadmin copy lại từ mẫu nếu muốn 14 mục).
-- KHÔNG sửa V1–V4. UPDATE có WHERE theo trạng thái trước V5 nên chạy
-- lại cũng không đổi gì; INSERT dùng SELECT ... WHERE NOT EXISTS như V4.
-- Toàn DML chuẩn SQL nên chạy được cả MySQL lẫn H2 (test sandbox).

-- 1. Đổi số bài cũ (từ số lớn xuống): Kết thúc 11->14, Ghép ngang 10->12,
--    Thay đổi số 9->11, Đường sắt 8->10, Ghép dọc 7->8. SORT_ORDER = LESSON_NO mới.
--    WHERE kèm NAME nên mỗi lệnh chỉ trúng đúng 1 bài.
UPDATE SH_LESSON SET LESSON_NO = 14, SORT_ORDER = 14
  WHERE PRESET_ID = 1 AND LESSON_NO = 11 AND NAME = 'Kết thúc';
UPDATE SH_LESSON SET LESSON_NO = 12, SORT_ORDER = 12
  WHERE PRESET_ID = 1 AND LESSON_NO = 10 AND NAME = 'Ghép xe ngang vào nơi đỗ';
UPDATE SH_LESSON SET LESSON_NO = 11, SORT_ORDER = 11
  WHERE PRESET_ID = 1 AND LESSON_NO = 9 AND NAME = 'Thay đổi số trên đường bằng';
UPDATE SH_LESSON SET LESSON_NO = 10, SORT_ORDER = 10
  WHERE PRESET_ID = 1 AND LESSON_NO = 8 AND NAME = 'Tạm dừng ở chỗ có đường sắt chạy qua';
UPDATE SH_LESSON SET LESSON_NO = 8, SORT_ORDER = 8
  WHERE PRESET_ID = 1 AND LESSON_NO = 7 AND NAME = 'Ghép xe dọc vào nơi đỗ';

-- 2. Bài 5 đổi tên theo màn hình chính thức (lời loa giữ nguyên).
UPDATE SH_LESSON SET NAME = 'Qua ngã tư (1)'
  WHERE PRESET_ID = 1 AND LESSON_NO = 5
  AND NAME = 'Qua ngã tư có đèn tín hiệu điều khiển giao thông';

-- 3. Thêm 3 lần qua ngã tư còn thiếu (20 giây như ngã tư (1)).
INSERT INTO SH_LESSON (PRESET_ID, LESSON_NO, NAME, ANNOUNCE_TEXT, DURATION_SECONDS, SORT_ORDER)
SELECT 1, 7, 'Qua ngã tư (2)', 'Bài thi qua ngã tư có đèn tín hiệu', 20, 7
WHERE NOT EXISTS (SELECT 1 FROM SH_LESSON WHERE PRESET_ID = 1 AND LESSON_NO = 7);
INSERT INTO SH_LESSON (PRESET_ID, LESSON_NO, NAME, ANNOUNCE_TEXT, DURATION_SECONDS, SORT_ORDER)
SELECT 1, 9, 'Qua ngã tư (3)', 'Bài thi qua ngã tư có đèn tín hiệu', 20, 9
WHERE NOT EXISTS (SELECT 1 FROM SH_LESSON WHERE PRESET_ID = 1 AND LESSON_NO = 9);
INSERT INTO SH_LESSON (PRESET_ID, LESSON_NO, NAME, ANNOUNCE_TEXT, DURATION_SECONDS, SORT_ORDER)
SELECT 1, 13, 'Qua ngã tư (4)', 'Bài thi qua ngã tư có đèn tín hiệu', 20, 13
WHERE NOT EXISTS (SELECT 1 FROM SH_LESSON WHERE PRESET_ID = 1 AND LESSON_NO = 13);

-- 4. Bước nhỏ cho 3 ngã tư mới (giống ngã tư (1): theo đèn + rẽ).
INSERT INTO SH_STEP (LESSON_ID, STEP_NO, NAME, ANNOUNCE_TEXT, DURATION_SECONDS, ENABLED, SORT_ORDER)
SELECT l.ID, v.STEP_NO, v.NAME, v.ANNOUNCE_TEXT, v.DURATION_SECONDS, 1, v.STEP_NO
FROM SH_LESSON l
JOIN (SELECT 1 AS STEP_NO, 'Dừng / đi theo đèn tín hiệu' AS NAME,
             'Qua ngã tư có đèn tín hiệu' AS ANNOUNCE_TEXT, 20 AS DURATION_SECONDS
      UNION ALL SELECT 2, 'Rẽ qua ngã tư', 'Qua ngã tư có đèn tín hiệu', 20) v
WHERE l.PRESET_ID = 1 AND l.LESSON_NO IN (7, 9, 13)
  AND NOT EXISTS (SELECT 1 FROM SH_STEP s WHERE s.LESSON_ID = l.ID AND s.STEP_NO = v.STEP_NO);

-- 5. Bảng lỗi: đổi LESSON_NO theo số bài mới.
UPDATE SH_ERROR_RULE SET LESSON_NO = 14
  WHERE PRESET_ID = 1 AND LESSON_NO = 11 AND CODE = 'NO_SIGNAL_RIGHT_END';
UPDATE SH_ERROR_RULE SET LESSON_NO = 12
  WHERE PRESET_ID = 1 AND LESSON_NO = 10 AND CODE = 'PARK_TIMEOUT';
UPDATE SH_ERROR_RULE SET LESSON_NO = 11
  WHERE PRESET_ID = 1 AND LESSON_NO = 9 AND CODE = 'WRONG_GEAR';
UPDATE SH_ERROR_RULE SET LESSON_NO = 8
  WHERE PRESET_ID = 1 AND LESSON_NO = 7 AND CODE = 'PARK_TIMEOUT';

-- 6. Lỗi ngã tư cho từng lần qua (màn hình chính thức: mỗi ngã tư đều có
--    "Quá 20 giây (-5đ)" và "Quá 30 giây loại trực tiếp"). Vượt đèn đỏ
--    (-10) và không xi nhan khi rẽ (-5) đã có ở bài 5, nhân bản cho 7/9/13;
--    2 lỗi quá giờ là mới, thêm cho cả 4 lần qua (5, 7, 9, 13).
INSERT INTO SH_ERROR_RULE (PRESET_ID, LESSON_NO, CODE, NAME, DEDUCT_POINTS, IS_LIET, SORT_ORDER)
SELECT 1, v.LESSON_NO, v.CODE, v.NAME, v.DEDUCT_POINTS, v.IS_LIET, v.SORT_ORDER
FROM (SELECT 5 AS LESSON_NO, 'SLOW_INTERSECTION_20S' AS CODE, 'Quá 20 giây chưa qua ngã tư' AS NAME, 5 AS DEDUCT_POINTS, 0 AS IS_LIET, 25 AS SORT_ORDER
      UNION ALL SELECT 5, 'NO_INTERSECTION_30S', 'Quá 30 giây chưa qua ngã tư', 0, 1, 26
      UNION ALL SELECT 7, 'RED_LIGHT', 'Vượt đèn đỏ', 10, 0, 27
      UNION ALL SELECT 7, 'NO_SIGNAL_TURN', 'Không bật xi nhan khi rẽ', 5, 0, 28
      UNION ALL SELECT 7, 'SLOW_INTERSECTION_20S', 'Quá 20 giây chưa qua ngã tư', 5, 0, 29
      UNION ALL SELECT 7, 'NO_INTERSECTION_30S', 'Quá 30 giây chưa qua ngã tư', 0, 1, 30
      UNION ALL SELECT 9, 'RED_LIGHT', 'Vượt đèn đỏ', 10, 0, 31
      UNION ALL SELECT 9, 'NO_SIGNAL_TURN', 'Không bật xi nhan khi rẽ', 5, 0, 32
      UNION ALL SELECT 9, 'SLOW_INTERSECTION_20S', 'Quá 20 giây chưa qua ngã tư', 5, 0, 33
      UNION ALL SELECT 9, 'NO_INTERSECTION_30S', 'Quá 30 giây chưa qua ngã tư', 0, 1, 34
      UNION ALL SELECT 13, 'RED_LIGHT', 'Vượt đèn đỏ', 10, 0, 35
      UNION ALL SELECT 13, 'NO_SIGNAL_TURN', 'Không bật xi nhan khi rẽ', 5, 0, 36
      UNION ALL SELECT 13, 'SLOW_INTERSECTION_20S', 'Quá 20 giây chưa qua ngã tư', 5, 0, 37
      UNION ALL SELECT 13, 'NO_INTERSECTION_30S', 'Quá 30 giây chưa qua ngã tư', 0, 1, 38) v
WHERE NOT EXISTS (SELECT 1 FROM SH_ERROR_RULE e
  WHERE e.PRESET_ID = 1 AND e.LESSON_NO = v.LESSON_NO AND e.CODE = v.CODE);

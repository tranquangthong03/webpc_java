-- Script SQL để sửa dữ liệu tour bị lỗi

-- 1. Kiểm tra các tour bị thiếu category_id
SELECT tour_id, tour_name, tour_code, category_id, destination_id, status
FROM tours
WHERE category_id IS NULL;

-- 2. Kiểm tra các tour bị thiếu destination_id
SELECT tour_id, tour_name, tour_code, category_id, destination_id, status
FROM tours
WHERE destination_id IS NULL;

-- 3. Kiểm tra các tour không có trạng thái ACTIVE
SELECT tour_id, tour_name, tour_code, category_id, destination_id, status
FROM tours
WHERE status != 'ACTIVE';

-- 4. Lấy thông tin category mặc định để gán cho tour thiếu category
SELECT TOP 1 category_id FROM categories WHERE status = 1 ORDER BY category_id;

-- 5. Lấy thông tin destination mặc định để gán cho tour thiếu destination
SELECT TOP 1 destination_id FROM destinations WHERE status = 1 ORDER BY destination_id;

-- 6. Cập nhật tour thiếu category_id
-- Thay {DEFAULT_CATEGORY_ID} bằng kết quả từ bước 4
UPDATE tours
SET category_id = 1
WHERE category_id IS NULL;

-- 7. Cập nhật tour thiếu destination_id
-- Thay {DEFAULT_DESTINATION_ID} bằng kết quả từ bước 5
UPDATE tours
SET destination_id = 1
WHERE destination_id IS NULL;

-- 8. Cập nhật trạng thái tour thành ACTIVE
UPDATE tours
SET status = 'ACTIVE'
WHERE status != 'ACTIVE' OR status IS NULL;

-- 9. Kiểm tra lại sau khi cập nhật
SELECT tour_id, tour_name, tour_code, category_id, destination_id, status
FROM tours
WHERE category_id IS NULL OR destination_id IS NULL OR status != 'ACTIVE';

-- 10. Kiểm tra toàn bộ tour có trạng thái ACTIVE
SELECT COUNT(*) FROM tours WHERE status = 'ACTIVE'; 
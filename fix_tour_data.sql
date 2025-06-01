-- Sửa các tour bị thiếu category_id, destination_id hoặc status không phải 'ACTIVE'
-- Kiểm tra các tour đang gặp vấn đề
SELECT tour_id, tour_name, category_id, destination_id, status 
FROM Tours 
WHERE category_id IS NULL OR destination_id IS NULL OR status != 'ACTIVE';

-- Cập nhật tất cả các tour không có category_id
UPDATE Tours SET category_id = 1 WHERE category_id IS NULL;

-- Cập nhật tất cả các tour không có destination_id
UPDATE Tours SET destination_id = 1 WHERE destination_id IS NULL;

-- Cập nhật tất cả các tour không có status ACTIVE
UPDATE Tours SET status = 'ACTIVE' WHERE status != 'ACTIVE';

-- Kiểm tra lại sau khi cập nhật
SELECT tour_id, tour_name, category_id, destination_id, status 
FROM Tours 
WHERE category_id IS NULL OR destination_id IS NULL OR status != 'ACTIVE';

-- Kiểm tra xem có category_id = 1 và destination_id = 1 tồn tại không
SELECT * FROM Categories WHERE category_id = 1;
SELECT * FROM Destinations WHERE destination_id = 1;

-- Nếu không tồn tại, hãy tạo mới
-- INSERT INTO Categories (category_id, category_name, description, status) 
-- VALUES (1, 'Tour du lịch', 'Danh mục mặc định', 1);

-- INSERT INTO Destinations (destination_id, destination_name, city, country, description, status) 
-- VALUES (1, 'Việt Nam', 'Hà Nội', 'Việt Nam', 'Điểm đến mặc định', 1); 
-- Thêm cột cancelled_at vào bảng Bookings (SQL Server)
ALTER TABLE Bookings
ADD cancelled_at DATETIME NULL; 
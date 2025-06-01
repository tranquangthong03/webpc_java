-- filepath: e:\WORK SPACE\travelwebsite\travelDb.sql
-- =============================================
-- HỆ THỐNG QUẢN LÝ DU LỊCH - DATABASE DESIGN
-- SQL Server Database Schema
-- =============================================

-- Kiểm tra và xóa database nếu đã tồn tại
USE master;
GO

IF EXISTS (SELECT name FROM master.dbo.sysdatabases WHERE name = N'TourismManagement')
BEGIN
    -- Đóng tất cả kết nối đến database
    ALTER DATABASE TourismManagement SET MULTI_USER WITH ROLLBACK IMMEDIATE;
    -- Xóa database
    DROP DATABASE TourismManagement;
END
GO

-- Tạo database mới
CREATE DATABASE TourismManagement
COLLATE Vietnamese_CI_AS;
GO

-- Sử dụng database vừa tạo
USE TourismManagement;
GO
ALTER DATABASE TourismManagement SET MULTI_USER;
GO
-- =============================================
-- 1. BẢNG NGƯỜI DÙNG (USERS)
-- =============================================
CREATE TABLE Users (
    user_id INT IDENTITY(1,1) PRIMARY KEY,
    username NVARCHAR(50) NOT NULL UNIQUE,
    email NVARCHAR(100) NOT NULL UNIQUE,
    password_hash NVARCHAR(255) NOT NULL,
    full_name NVARCHAR(100) NOT NULL,
    phone NVARCHAR(15),
    address NVARCHAR(255),
    date_of_birth DATE,
    gender NVARCHAR(10) CHECK (gender IN ('Male', 'Female', 'Other')),
    avatar_url NVARCHAR(255),
    role NVARCHAR(20) NOT NULL DEFAULT 'CUSTOMER' CHECK (role IN ('ADMIN', 'STAFF', 'CUSTOMER')),
    status NVARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'BANNED')),
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE(),
    last_login DATETIME2
);

-- =============================================
-- 2. BẢNG DANH MỤC TOUR (CATEGORIES)
-- =============================================
CREATE TABLE Categories (
    category_id INT IDENTITY(1,1) PRIMARY KEY,
    category_name NVARCHAR(100) NOT NULL UNIQUE,
    description NVARCHAR(500),
    icon_url NVARCHAR(255),
    status BIT NOT NULL DEFAULT 1,
    created_at DATETIME2 DEFAULT GETDATE()
);

-- =============================================
-- 3. BẢNG ĐIỂM ĐẾN (DESTINATIONS)
-- =============================================
CREATE TABLE Destinations (
    destination_id INT IDENTITY(1,1) PRIMARY KEY,
    destination_name NVARCHAR(100) NOT NULL,
    country NVARCHAR(50) NOT NULL,
    city NVARCHAR(50) NOT NULL,
    description NVARCHAR(1000),
    image_url NVARCHAR(255),
    latitude DECIMAL(10,8),
    longitude DECIMAL(11,8),
    status BIT NOT NULL DEFAULT 1,
    created_at DATETIME2 DEFAULT GETDATE()
);

-- =============================================
-- 4. BẢNG TOUR
-- =============================================
CREATE TABLE Tours (
    tour_id INT IDENTITY(1,1) PRIMARY KEY,
    tour_name NVARCHAR(200) NOT NULL,
    tour_code NVARCHAR(20) NOT NULL UNIQUE,
    category_id INT NOT NULL,
    destination_id INT NOT NULL,
    description NVARCHAR(2000),
    duration_days INT NOT NULL CHECK (duration_days > 0),
    max_participants INT NOT NULL CHECK (max_participants > 0),
    price DECIMAL(18,2) NOT NULL CHECK (price >= 0),
    discount_percentage DECIMAL(5,2) DEFAULT 0 CHECK (discount_percentage >= 0 AND discount_percentage <= 100),
    main_image_url NVARCHAR(255),
    difficulty_level NVARCHAR(20) CHECK (difficulty_level IN ('Easy', 'Medium', 'Hard')),
    includes NVARCHAR(1000), -- Bao gồm gì trong tour
    excludes NVARCHAR(1000), -- Không bao gồm gì
    status NVARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'DRAFT')),
    featured BIT DEFAULT 0, -- Tour nổi bật
    created_by INT NOT NULL,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE(),
    
    CONSTRAINT FK_Tours_Categories FOREIGN KEY (category_id) REFERENCES Categories(category_id),
    CONSTRAINT FK_Tours_Destinations FOREIGN KEY (destination_id) REFERENCES Destinations(destination_id),
    CONSTRAINT FK_Tours_CreatedBy FOREIGN KEY (created_by) REFERENCES Users(user_id)
);

-- =============================================
-- 5. BẢNG HÌNH ẢNH TOUR
-- =============================================
CREATE TABLE TourImages (
    image_id INT IDENTITY(1,1) PRIMARY KEY,
    tour_id INT NOT NULL,
    image_url NVARCHAR(255) NOT NULL,
    image_title NVARCHAR(100),
    is_main BIT DEFAULT 0,
    display_order INT DEFAULT 0,
    created_at DATETIME2 DEFAULT GETDATE(),
    
    CONSTRAINT FK_TourImages_Tours FOREIGN KEY (tour_id) REFERENCES Tours(tour_id) ON DELETE CASCADE
);

-- =============================================
-- 6. BẢNG LỊCH TRÌNH TOUR
-- =============================================
CREATE TABLE TourItineraries (
    itinerary_id INT IDENTITY(1,1) PRIMARY KEY,
    tour_id INT NOT NULL,
    day_number INT NOT NULL CHECK (day_number > 0),
    title NVARCHAR(200) NOT NULL,
    description NVARCHAR(2000),
    activities NVARCHAR(1000),
    meals NVARCHAR(200), -- Bữa ăn trong ngày
    accommodation NVARCHAR(200), -- Nơi ở
    created_at DATETIME2 DEFAULT GETDATE(),
    
    CONSTRAINT FK_TourItineraries_Tours FOREIGN KEY (tour_id) REFERENCES Tours(tour_id) ON DELETE CASCADE,
    CONSTRAINT UK_TourItineraries_TourDay UNIQUE (tour_id, day_number)
);

-- =============================================
-- 7. BẢNG LỊCH KHỞI HÀNH
-- =============================================
CREATE TABLE TourSchedules (
    schedule_id INT IDENTITY(1,1) PRIMARY KEY,
    tour_id INT NOT NULL,
    departure_date DATE NOT NULL,
    return_date DATE NOT NULL,
    available_slots INT NOT NULL CHECK (available_slots >= 0),
    booked_slots INT DEFAULT 0 CHECK (booked_slots >= 0),
    price DECIMAL(18,2) NOT NULL CHECK (price >= 0),
    status NVARCHAR(20) NOT NULL DEFAULT 'AVAILABLE' CHECK (status IN ('AVAILABLE', 'FULL', 'CANCELLED', 'COMPLETED')),
    guide_name NVARCHAR(100),
    guide_phone NVARCHAR(15),
    meeting_point NVARCHAR(255),
    special_notes NVARCHAR(500),
    created_at DATETIME2 DEFAULT GETDATE(),
    
    CONSTRAINT FK_TourSchedules_Tours FOREIGN KEY (tour_id) REFERENCES Tours(tour_id),
    CONSTRAINT CHK_TourSchedules_Dates CHECK (return_date >= departure_date),
    CONSTRAINT CHK_TourSchedules_Slots CHECK (booked_slots <= available_slots)
);

-- =============================================
-- 8. BẢNG ĐẶT TOUR (BOOKINGS)
-- =============================================
CREATE TABLE Bookings (
    booking_id INT IDENTITY(1,1) PRIMARY KEY,
    booking_code NVARCHAR(20) NOT NULL UNIQUE,
    user_id INT NOT NULL,
    schedule_id INT NOT NULL,
    adult_count INT NOT NULL DEFAULT 1 CHECK (adult_count > 0),
    child_count INT DEFAULT 0 CHECK (child_count >= 0),
    total_amount DECIMAL(18,2) NOT NULL CHECK (total_amount >= 0),
    discount_amount DECIMAL(18,2) DEFAULT 0 CHECK (discount_amount >= 0),
    final_amount DECIMAL(18,2) NOT NULL CHECK (final_amount >= 0),
    booking_status NVARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (booking_status IN ('PENDING', 'CONFIRMED', 'PAID', 'CANCELLED', 'COMPLETED')),
    payment_status NVARCHAR(20) NOT NULL DEFAULT 'UNPAID' CHECK (payment_status IN ('UNPAID', 'PARTIAL', 'PAID', 'REFUNDED')),
    customer_notes NVARCHAR(500),
    admin_notes NVARCHAR(500),
    booking_date DATETIME2 DEFAULT GETDATE(),
    confirmed_date DATETIME2,
    cancelled_date DATETIME2,
    cancellation_reason NVARCHAR(500),
    
    CONSTRAINT FK_Bookings_Users FOREIGN KEY (user_id) REFERENCES Users(user_id),
    CONSTRAINT FK_Bookings_Schedules FOREIGN KEY (schedule_id) REFERENCES TourSchedules(schedule_id)
);

-- =============================================
-- 9. BẢNG THÔNG TIN KHÁCH HÀNG THAM GIA
-- =============================================
CREATE TABLE BookingParticipants (
    participant_id INT IDENTITY(1,1) PRIMARY KEY,
    booking_id INT NOT NULL,
    full_name NVARCHAR(100) NOT NULL,
    date_of_birth DATE,
    gender NVARCHAR(10) CHECK (gender IN ('Male', 'Female', 'Other')),
    id_number NVARCHAR(20), -- CMND/CCCD
    phone NVARCHAR(15),
    email NVARCHAR(100),
    participant_type NVARCHAR(10) NOT NULL CHECK (participant_type IN ('Adult', 'Child')),
    special_requirements NVARCHAR(300), -- Yêu cầu đặc biệt (ăn chay, dị ứng...)
    
    CONSTRAINT FK_BookingParticipants_Bookings FOREIGN KEY (booking_id) REFERENCES Bookings(booking_id) ON DELETE CASCADE
);

-- =============================================
-- 10. BẢNG THANH TOÁN
-- =============================================
CREATE TABLE Payments (
    payment_id INT IDENTITY(1,1) PRIMARY KEY,
    booking_id INT NOT NULL,
    payment_method NVARCHAR(20) NOT NULL CHECK (payment_method IN ('CASH', 'BANK_TRANSFER', 'VNPAY', 'MOMO', 'ZALOPAY', 'CREDIT_CARD')),
    amount DECIMAL(18,2) NOT NULL CHECK (amount > 0),
    transaction_id NVARCHAR(100), -- ID giao dịch từ cổng thanh toán
    payment_status NVARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (payment_status IN ('PENDING', 'SUCCESS', 'FAILED', 'CANCELLED')),
    payment_date DATETIME2 DEFAULT GETDATE(),
    processed_date DATETIME2,
    failure_reason NVARCHAR(255),
    notes NVARCHAR(500),
    
    CONSTRAINT FK_Payments_Bookings FOREIGN KEY (booking_id) REFERENCES Bookings(booking_id)
);

-- Thêm dữ liệu mẫu cho Users
INSERT INTO Users (username, email, password_hash, full_name, phone, address, date_of_birth, gender, role, status) VALUES
('admin', 'admin@tourism.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKXYLFSaZNqsoYgGvZCTrAp/YCFG', N'Quản trị viên', '0901234567', N'123 Nguyễn Huệ, Q1, TP.HCM', '1985-01-15', 'Male', 'ADMIN', 'ACTIVE'),
('staff01', 'staff01@tourism.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKXYLFSaZNqsoYgGvZCTrAp/YCFG', N'Nhân viên Nguyễn Văn A', '0902345678', N'456 Lê Lợi, Q1, TP.HCM', '1990-05-20', 'Male', 'STAFF', 'ACTIVE'),
('customer01', 'customer01@gmail.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKXYLFSaZNqsoYgGvZCTrAp/YCFG', N'Trần Thị B', '0903456789', N'789 Nguyễn Trãi, Q5, TP.HCM', '1995-08-10', 'Female', 'CUSTOMER', 'ACTIVE'),
('customer02', 'customer02@gmail.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKXYLFSaZNqsoYgGvZCTrAp/YCFG', N'Lê Văn C', '0904567890', N'321 Cách Mạng Tháng 8, Q10, TP.HCM', '1988-12-25', 'Male', 'CUSTOMER', 'ACTIVE'),
('customer03', 'customer03@gmail.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKXYLFSaZNqsoYgGvZCTrAp/YCFG', N'Phạm Thị D', '0905678901', N'654 Võ Văn Tần, Q3, TP.HCM', '1992-03-18', 'Female', 'CUSTOMER', 'ACTIVE');

-- Thêm dữ liệu mẫu cho Categories
INSERT INTO Categories (category_name, description, icon_url) VALUES
(N'Du lịch biển', N'Các tour du lịch biển, nghỉ dưỡng tại các bãi biển đẹp', '/images/icons/beach.png'),
(N'Du lịch núi', N'Các tour leo núi, trekking, khám phá thiên nhiên', '/images/icons/mountain.png'),
(N'Du lịch văn hóa', N'Khám phá văn hóa, lịch sử, di tích', '/images/icons/culture.png'),
(N'Du lịch phiêu lưu', N'Các hoạt động mạo hiểm, thể thao cực hạn', '/images/icons/adventure.png'),
(N'Du lịch ẩm thực', N'Khám phá ẩm thực địa phương', '/images/icons/food.png'),
(N'Du lịch nghỉ dưỡng', N'Thư giãn, spa, resort cao cấp', '/images/icons/resort.png');

-- Thêm dữ liệu mẫu cho Destinations
INSERT INTO Destinations (destination_name, country, city, description, image_url, latitude, longitude) VALUES
(N'Vịnh Hạ Long', N'Việt Nam', N'Quảng Ninh', N'Di sản thiên nhiên thế giới với hàng ngàn hòn đảo đá vôi', '/images/destinations/halong.jpg', 20.9101, 107.1839),
(N'Hội An', N'Việt Nam', N'Quảng Nam', N'Phố cổ với kiến trúc độc đáo, đèn lồng rực rỡ', '/images/destinations/hoian.jpg', 15.8801, 108.3380),
(N'Sapa', N'Việt Nam', N'Lào Cai', N'Ruộng bậc thang tuyệt đẹp, khí hậu mát mẻ', '/images/destinations/sapa.jpg', 22.3364, 103.8438),
(N'Phú Quốc', N'Việt Nam', N'Kiên Giang', N'Đảo ngọc với bãi biển trong xanh, hải sản tươi ngon', '/images/destinations/phuquoc.jpg', 10.2895, 103.9845),
(N'Đà Lạt', N'Việt Nam', N'Lâm Đồng', N'Thành phố ngàn hoa với khí hậu mát mẻ quanh năm', '/images/destinations/dalat.jpg', 11.9404, 108.4583),
(N'Nha Trang', N'Việt Nam', N'Khánh Hòa', N'Bãi biển đẹp, hoạt động thể thao biển phong phú', '/images/destinations/nhatrang.jpg', 12.2388, 109.1967);

-- Thêm dữ liệu mẫu cho Tours
INSERT INTO Tours (tour_name, tour_code, category_id, destination_id, description, duration_days, max_participants, price, discount_percentage, main_image_url, difficulty_level, includes, excludes, status, featured, created_by) VALUES
(N'Vịnh Hạ Long - Đảo Cát Bà 3N2Đ', 'HL001', 1, 1, N'Khám phá vẻ đẹp hùng vĩ của Vịnh Hạ Long, tham quan đảo Cát Bà, thưởng thức hải sản tươi ngon', 3, 25, 2500000, 10, '/images/tours/halong-catba.jpg', 'Easy', N'Xe du lịch, khách sạn 3*, ăn sáng, hướng dẫn viên', N'Chi phí cá nhân, đồ uống', 'ACTIVE', 1, 1),
(N'Hội An - Mỹ Sơn 2N1Đ', 'HA001', 3, 2, N'Tham quan phố cổ Hội An, khám phá thánh địa Mỹ Sơn cổ kính', 2, 20, 1800000, 5, '/images/tours/hoian-myson.jpg', 'Easy', N'Xe du lịch, khách sạn 3*, ăn sáng, vé tham quan', N'Ăn trưa, ăn tối', 'ACTIVE', 1, 1),
(N'Sapa - Fansipan 4N3Đ', 'SP001', 2, 3, N'Chinh phục đỉnh Fansipan, trekking qua các bản làng dân tộc', 4, 15, 3200000, 0, '/images/tours/sapa-fansipan.jpg', 'Hard', N'Xe du lịch, homestay, ăn 3 bữa/ngày, hướng dẫn viên chuyên nghiệp', N'Cáp treo Fansipan, đồ uống', 'ACTIVE', 1, 1),
(N'Phú Quốc - Nghỉ dưỡng 4N3Đ', 'PQ001', 6, 4, N'Nghỉ dưỡng tại resort 5*, tắm biển, thưởng thức hải sản', 4, 30, 4500000, 15, '/images/tours/phuquoc-resort.jpg', 'Easy', N'Vé máy bay, resort 5*, ăn buffet sáng, xe đưa đón sân bay', N'Ăn trưa, ăn tối, chi phí cá nhân', 'ACTIVE', 1, 1),
(N'Đà Lạt - Thành phố ngàn hoa 3N2Đ', 'DL001', 5, 5, N'Khám phá thành phố ngàn hoa, thưởng thức đặc sản địa phương', 3, 25, 2200000, 8, '/images/tours/dalat-flower.jpg', 'Easy', N'Xe du lịch, khách sạn 3*, ăn sáng, hướng dẫn viên', N'Ăn trưa, ăn tối, vé tham quan', 'ACTIVE', 0, 1),
(N'Nha Trang - Biển xanh cát trắng 3N2Đ', 'NT001', 1, 6, N'Tắm biển, lặn ngắm san hô, thưởng thức hải sản tươi ngon', 3, 30, 2800000, 12, '/images/tours/nhatrang-beach.jpg', 'Easy', N'Xe du lịch, khách sạn 4*, ăn sáng, tour lặn biển', N'Ăn trưa, ăn tối, đồ uống', 'ACTIVE', 1, 1);

-- Thêm dữ liệu mẫu cho TourSchedules
INSERT INTO TourSchedules (tour_id, departure_date, return_date, available_slots, booked_slots, price, status, guide_name, guide_phone, meeting_point) VALUES
(1, '2025-06-15', '2025-06-17', 25, 0, 2250000, 'AVAILABLE', N'Nguyễn Văn Guide', '0911111111', N'Văn phòng du lịch - 123 Nguyễn Huệ, Q1, TP.HCM'),
(1, '2025-07-01', '2025-07-03', 25, 5, 2250000, 'AVAILABLE', N'Trần Thị Hướng', '0922222222', N'Văn phòng du lịch - 123 Nguyễn Huệ, Q1, TP.HCM'),
(2, '2025-06-20', '2025-06-21', 20, 0, 1710000, 'AVAILABLE', N'Lê Văn Nam', '0933333333', N'Sân bay Tân Sơn Nhất'),
(3, '2025-08-10', '2025-08-13', 15, 3, 3200000, 'AVAILABLE', N'Phạm Minh Tuấn', '0944444444', N'Ga Hà Nội'),
(4, '2025-07-15', '2025-07-18', 30, 0, 3825000, 'AVAILABLE', N'Võ Thị Mai', '0955555555', N'Sân bay Tân Sơn Nhất'),
(5, '2025-06-25', '2025-06-27', 25, 2, 2024000, 'AVAILABLE', N'Hoàng Văn Dũng', '0966666666', N'Văn phòng du lịch - 123 Nguyễn Huệ, Q1, TP.HCM'),
(6, '2025-07-05', '2025-07-07', 30, 8, 2464000, 'AVAILABLE', N'Ngô Thị Lan', '0977777777', N'Sân bay Cam Ranh');

-- Thêm dữ liệu mẫu cho TourItineraries
INSERT INTO TourItineraries (tour_id, day_number, title, description, activities, meals, accommodation) VALUES
-- Tour Hạ Long (tour_id = 1)
(1, 1, N'Ngày 1: TP.HCM - Hà Nội - Hạ Long', N'Khởi hành từ TP.HCM đi Hà Nội, sau đó di chuyển đến Hạ Long', N'Bay từ TP.HCM, di chuyển xe khách đến Hạ Long, nhận phòng khách sạn', N'Ăn tối', N'Khách sạn 3* tại Hạ Long'),
(1, 2, N'Ngày 2: Tham quan Vịnh Hạ Long', N'Du thuyền tham quan Vịnh Hạ Long, hang Sửng Sốt, đảo Titop', N'Du thuyền, tham quan hang động, tắm biển, chèo kayak', N'Ăn sáng, ăn trưa, ăn tối', N'Tàu du lịch nghỉ đêm'),
(1, 3, N'Ngày 3: Đảo Cát Bà - Về TP.HCM', N'Tham quan đảo Cát Bà, về Hà Nội và bay về TP.HCM', N'Tham quan đảo Cát Bà, mua sắm đặc sản', N'Ăn sáng, ăn trưa', N'Máy bay về TP.HCM'),

-- Tour Hội An (tour_id = 2)
(2, 1, N'Ngày 1: TP.HCM - Đà Nẵng - Hội An', N'Bay từ TP.HCM đến Đà Nẵng, di chuyển đến Hội An tham quan phố cổ', N'Bay đến Đà Nẵng, tham quan phố cổ Hội An, chùa Cầu', N'Ăn tối', N'Khách sạn 3* tại Hội An'),
(2, 2, N'Ngày 2: Mỹ Sơn - Về TP.HCM', N'Tham quan thánh địa Mỹ Sơn, về Đà Nẵng và bay về TP.HCM', N'Tham quan Mỹ Sơn, mua sắm tại chợ Hội An', N'Ăn sáng, ăn trưa', N'Máy bay về TP.HCM');

-- Thêm dữ liệu mẫu cho Bookings
INSERT INTO Bookings (booking_code, user_id, schedule_id, adult_count, child_count, total_amount, discount_amount, final_amount, booking_status, payment_status, customer_notes) VALUES
('BK000001', 3, 2, 2, 1, 5130000, 513000, 4617000, 'CONFIRMED', 'PAID', N'Yêu cầu phòng đôi'),
('BK000002', 4, 4, 1, 0, 3200000, 0, 3200000, 'PENDING', 'UNPAID', N'Liên hệ trước khi khởi hành'),
('BK000003', 5, 6, 2, 0, 4048000, 0, 4048000, 'CONFIRMED', 'PARTIAL', N'Đã thanh toán 50%');

-- Thêm dữ liệu mẫu cho BookingParticipants
INSERT INTO BookingParticipants (booking_id, full_name, date_of_birth, gender, id_number, phone, email, participant_type, special_requirements) VALUES
(1, N'Trần Thị B', '1995-08-10', 'Female', '079095012345', '0903456789', 'customer01@gmail.com', 'Adult', N'Ăn chay'),
(1, N'Nguyễn Văn X', '1993-05-15', 'Male', '079093054321', '0903456788', '', 'Adult', ''),
(1, N'Trần Thị Y', '2018-02-20', 'Female', '', '', '', 'Child', N'Dị ứng hải sản'),
(2, N'Lê Văn C', '1988-12-25', 'Male', '079088098765', '0904567890', 'customer02@gmail.com', 'Adult', ''),
(3, N'Phạm Thị D', '1992-03-18', 'Female', '079092087654', '0905678901', 'customer03@gmail.com', 'Adult', ''),
(3, N'Phạm Văn Z', '1990-07-22', 'Male', '079090076543', '0905678902', '', 'Adult', '');

-- Thêm dữ liệu mẫu cho Payments
INSERT INTO Payments (booking_id, payment_method, amount, transaction_id, payment_status, payment_date, processed_date) VALUES
(1, 'VNPAY', 4617000, 'VNP_20250524_001', 'SUCCESS', '2025-05-20 14:30:00', '2025-05-20 14:31:15'),
(3, 'BANK_TRANSFER', 2024000, 'BT_20250523_001', 'SUCCESS', '2025-05-23 09:15:00', '2025-05-23 09:16:30');

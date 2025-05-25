# Travel Web Application

Ứng dụng web du lịch được xây dựng bằng Java Spring Boot theo mô hình MVC và kiến trúc 3-layer.

## Công nghệ sử dụng

- **Java 17**
- **Spring Boot 3.2.5**
- **Spring Data JPA**
- **Thymeleaf** (Template Engine)
- **MySQL** (Database)
- **Maven** (Build Tool)

## Kiến trúc dự án

```
src/
├── main/
│   ├── java/com/example/travelweb/
│   │   ├── TravelWebApplication.java    # Main class
│   │   ├── controller/                  # Presentation Layer
│   │   ├── service/                     # Business Layer
│   │   ├── repository/                  # Data Access Layer
│   │   ├── entity/                      # Model entities
│   │   ├── dto/                         # Data Transfer Objects
│   │   └── config/                      # Configuration classes
│   └── resources/
│       ├── templates/                   # Thymeleaf templates
│       ├── static/                      # CSS, JS, Images
│       └── application.properties       # Configuration
└── test/                                # Unit tests
```

## Cài đặt và chạy

1. Clone repository
2. Cấu hình database trong `application.properties`
3. Chạy lệnh: `mvn spring-boot:run`
4. Truy cập: http://localhost:8080

## Tính năng

- Hiển thị danh sách tour du lịch
- Tìm kiếm và lọc tour
- Xem chi tiết tour
- Quản lý đặt tour
- Quản trị viên (CRUD operations)

## Database

Sử dụng cơ sở dữ liệu MySQL với các bảng chính:
- Tours
- Customers
- Bookings
- Categories
- Users

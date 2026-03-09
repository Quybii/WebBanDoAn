# BƯỚC 1 – TÓM TẮT: THIẾT KẾ DATABASE VÀ ENTITY

## Đã hoàn thành

### 1. Script SQL (`sql/schema.sql`)

- **roles:** id, name (USER, ADMIN)
- **users:** id, username, password, full_name, email, phone, address, role_id, enabled, created_at
- **categories:** id, name, description, image_url
- **foods:** id, name, description, price, image_url, category_id, is_available, created_at
- **orders:** id, user_id, order_date, total_amount, status, shipping_address, phone, note
- **order_details:** id, order_id, food_id, quantity, unit_price, subtotal
- **cart_items:** id, user_id, food_id, quantity, created_at (UNIQUE user_id + food_id)

Có kèm dữ liệu mẫu (roles, users, categories, foods). Mật khẩu seed cần mã hóa BCrypt khi chạy app (hoặc dùng chức năng Đăng ký).

**Cách chạy:** Mở SQL Server Management Studio (hoặc Azure Data Studio), kết nối tới SQL Server, tạo database (ví dụ `webbandoan`), chọn database đó rồi thực thi nội dung file `sql/schema.sql`.

### 2. Giải thích quan hệ (`docs/BƯỚC1_QUAN_HỆ_DATABASE.md`)

- **ManyToOne:** User → Role, Food → Category, Order → User, OrderDetail → Order/Food, CartItem → User/Food
- **OneToMany:** Role → Users, Category → Foods, Order → OrderDetails, User → Orders/CartItems, Food → OrderDetails/CartItems

### 3. Entity Java (package `com.webbandoan.entity`)

| File           | Bảng          | Annotation chính                                      |
|----------------|---------------|--------------------------------------------------------|
| Role.java      | roles         | @Entity, @Id, @GeneratedValue, @OneToMany(mappedBy)    |
| User.java      | users         | @ManyToOne Role, @OneToMany Order/CartItem             |
| Category.java  | categories    | @Entity, @OneToMany Food                               |
| Food.java      | foods         | @ManyToOne Category, @OneToMany OrderDetail/CartItem   |
| Order.java     | orders        | @ManyToOne User, @OneToMany OrderDetail                 |
| OrderDetail.java | order_details | @ManyToOne Order, @ManyToOne Food                    |
| CartItem.java  | cart_items    | @ManyToOne User, @ManyToOne Food, @UniqueConstraint    |

- Dùng **Jakarta Persistence** (`jakarta.persistence.*`) cho Spring Boot 3.
- Khóa chính: `@GeneratedValue(strategy = GenerationType.IDENTITY)` (phù hợp SQL Server IDENTITY).
- Quan hệ **lazy** (`FetchType.LAZY`) ở phía ManyToOne để tránh load dữ liệu thừa.
- `OrderDetail` có constructor tiện: nhận order, food, quantity, unitPrice và tự tính `subtotal`.

## Bước tiếp theo

Sang **BƯỚC 2 – CẤU TRÚC PROJECT**: tạo project Spring Boot (pom.xml / build.gradle), cấu hình SQL Server, và các thư mục controller, service, repository, config, templates, static.

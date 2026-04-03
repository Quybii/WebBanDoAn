-- =====================================================
-- WEBBANDOAN - SCRIPT TẠO BẢNG DATABASE (SQL SERVER)
-- Bước 1: Thiết kế Database
-- =====================================================

-- Xóa bảng theo thứ tự ngược phụ thuộc (tránh lỗi FK)
IF OBJECT_ID('dbo.payment_transactions', 'U') IS NOT NULL DROP TABLE dbo.payment_transactions;
IF OBJECT_ID('dbo.cart_items', 'U') IS NOT NULL DROP TABLE dbo.cart_items;
IF OBJECT_ID('dbo.order_details', 'U') IS NOT NULL DROP TABLE dbo.order_details;
IF OBJECT_ID('dbo.orders', 'U') IS NOT NULL DROP TABLE dbo.orders;
IF OBJECT_ID('dbo.foods', 'U') IS NOT NULL DROP TABLE dbo.foods;
IF OBJECT_ID('dbo.categories', 'U') IS NOT NULL DROP TABLE dbo.categories;
IF OBJECT_ID('dbo.users', 'U') IS NOT NULL DROP TABLE dbo.users;
IF OBJECT_ID('dbo.roles', 'U') IS NOT NULL DROP TABLE dbo.roles;
IF OBJECT_ID('dbo.payment_methods', 'U') IS NOT NULL DROP TABLE dbo.payment_methods;

-- -----------------------------------------------------
-- 1. BẢNG roles (Vai trò: USER, ADMIN)
-- -----------------------------------------------------
CREATE TABLE roles (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(50) NOT NULL UNIQUE
);

-- Bảng payment_methods (Phương thức thanh toán)
-- COD: Thanh toán khi nhận hàng
-- MOMO: Chuyển khoản Momo
CREATE TABLE payment_methods (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    code NVARCHAR(50) NOT NULL UNIQUE,
    name NVARCHAR(200) NOT NULL,
    description NVARCHAR(500),
    is_active BIT NOT NULL DEFAULT 1
);

-- INSERT INTO roles (Vai trò: USER, ADMIN)
-- INSERT INTO payment_methods

-- -----------------------------------------------------
-- 2. BẢNG users (Người dùng)
-- Quan hệ: Many users -> One role (ManyToOne với roles)
-- -----------------------------------------------------
CREATE TABLE users (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    username NVARCHAR(100) NOT NULL UNIQUE,
    password NVARCHAR(255) NOT NULL,
    full_name NVARCHAR(200),
    email NVARCHAR(150),
    phone NVARCHAR(20),
    address NVARCHAR(500),
    role_id BIGINT NOT NULL,
    enabled BIT NOT NULL DEFAULT 1,
    created_at DATETIME2 DEFAULT GETDATE(),
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- -----------------------------------------------------
-- 3. BẢNG categories (Danh mục món ăn)
-- -----------------------------------------------------
CREATE TABLE categories (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(200) NOT NULL,
    description NVARCHAR(500),
    image_url NVARCHAR(500)
);

-- -----------------------------------------------------
-- 4. BẢNG foods (Món ăn)
-- Quan hệ: Many foods -> One category (ManyToOne với categories)
-- -----------------------------------------------------
CREATE TABLE foods (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(200) NOT NULL,
    description NVARCHAR(1000),
    price DECIMAL(12,2) NOT NULL CHECK (price >= 0),
    image_url NVARCHAR(500),
    category_id BIGINT NOT NULL,
    is_available BIT NOT NULL DEFAULT 1,
    created_at DATETIME2 DEFAULT GETDATE(),
    CONSTRAINT fk_foods_category FOREIGN KEY (category_id) REFERENCES categories(id)
);

-- -----------------------------------------------------
-- 5. BẢNG orders (Đơn hàng)
-- Quan hệ: Many orders -> One user (ManyToOne với users)
--          Many orders -> One payment_method (ManyToOne với payment_methods)
-- -----------------------------------------------------
CREATE TABLE orders (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    order_date DATETIME2 NOT NULL DEFAULT GETDATE(),
    total_amount DECIMAL(12,2) NOT NULL CHECK (total_amount >= 0),
    status NVARCHAR(50) NOT NULL DEFAULT N'PENDING',
    shipping_address NVARCHAR(500) NOT NULL,
    phone NVARCHAR(20) NOT NULL,
    note NVARCHAR(500),
    payment_method_id BIGINT NOT NULL DEFAULT 1,  -- Mặc định COD (id=1)
    payment_status NVARCHAR(50) NOT NULL DEFAULT N'PENDING',  -- PENDING, COMPLETED, FAILED, CANCELLED
    transaction_id NVARCHAR(100),  -- Mã giao dịch từ Momo (nếu thanh toán Momo)
    CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_orders_payment_method FOREIGN KEY (payment_method_id) REFERENCES payment_methods(id)
);

-- -----------------------------------------------------
-- 6. BẢNG order_details (Chi tiết đơn hàng)
-- Quan hệ: Many order_details -> One order (ManyToOne)
--          Many order_details -> One food (ManyToOne)
-- -----------------------------------------------------
CREATE TABLE order_details (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    order_id BIGINT NOT NULL,
    food_id BIGINT NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(12,2) NOT NULL CHECK (unit_price >= 0),
    subtotal DECIMAL(12,2) NOT NULL CHECK (subtotal >= 0),
    CONSTRAINT fk_order_details_order FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT fk_order_details_food FOREIGN KEY (food_id) REFERENCES foods(id)
);

-- Bảng payment_transactions (Lưu thông tin giao dịch thanh toán - dành cho Momo)
-- Tương tự bảng MomoInfor cũ của bạn
-- Một đơn hàng có thể có nhiều giao dịch (nếu thanh toán nhiều lần/retry)
CREATE TABLE payment_transactions (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    order_id BIGINT NOT NULL,
    momo_transaction_id NVARCHAR(100),  -- ID giao dịch từ Momo
    momo_request_id NVARCHAR(100),      -- Request ID gửi tới Momo
    order_info NVARCHAR(500),            -- Mô tả giao dịch
    amount DECIMAL(12,2) NOT NULL,       -- Số tiền giao dịch
    payment_status NVARCHAR(50),         -- PENDING, SUCCESS, FAILED, CANCELLED
    created_date DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_date DATETIME2,
    response_code INT,                   -- Response code từ Momo API
    response_message NVARCHAR(500),      -- Response message từ Momo API
    CONSTRAINT fk_payment_trans_order FOREIGN KEY (order_id) REFERENCES orders(id)
);

-- -----------------------------------------------------
-- 7. BẢNG cart_items (Giỏ hàng)
-- Quan hệ: Many cart_items -> One user (ManyToOne)
--          Many cart_items -> One food (ManyToOne)
-- Mỗi user chỉ có tối đa 1 dòng cho mỗi món (unique user_id + food_id)
-- -----------------------------------------------------
CREATE TABLE cart_items (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    food_id BIGINT NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0) DEFAULT 1,
    created_at DATETIME2 DEFAULT GETDATE(),
    CONSTRAINT fk_cart_items_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_cart_items_food FOREIGN KEY (food_id) REFERENCES foods(id),
    CONSTRAINT uq_cart_user_food UNIQUE (user_id, food_id)
);

-- -----------------------------------------------------
-- INDEX để tăng tốc truy vấn
-- -----------------------------------------------------
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_role_id ON users(role_id);
CREATE INDEX idx_foods_category_id ON foods(category_id);
CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_order_date ON orders(order_date);
CREATE INDEX idx_orders_payment_method ON orders(payment_method_id);
CREATE INDEX idx_orders_payment_status ON orders(payment_status);
CREATE INDEX idx_order_details_order_id ON order_details(order_id);
CREATE INDEX idx_cart_items_user_id ON cart_items(user_id);
CREATE INDEX idx_payment_transactions_order_id ON payment_transactions(order_id);
CREATE INDEX idx_payment_transactions_momo_id ON payment_transactions(momo_transaction_id);

-- -----------------------------------------------------
-- DỮ LIỆU MẪU (Seed data)
-- -----------------------------------------------------
INSERT INTO roles (name) VALUES (N'USER'), (N'ADMIN');

-- Thêm 2 phương thức thanh toán: COD và MOMO
INSERT INTO payment_methods (code, name, description, is_active) VALUES
    (N'COD', N'Thanh toán khi nhận hàng', N'Thanh toán tiền mặt khi nhận hàng', 1),
    (N'MOMO', N'Chuyển khoản MoMo', N'Thanh toán qua ứng dụng MoMo', 1);

-- Password mẫu: 123456 (BCrypt hash - tạo bằng BCryptPasswordEncoder trong app nếu cần)
INSERT INTO users (username, password, full_name, email, phone, address, role_id, enabled)
VALUES 
    (N'admin', N'$2a$10$8K1p/a0dL1LXMIgoEDFrwOfMQD4wYqgJV/xQYqVvVqVvVqVvVqVvV', N'Quản trị viên', N'admin@webbandoan.com', N'0901234567', N'Admin Address', 2, 1),
    (N'user1', N'$2a$10$8K1p/a0dL1LXMIgoEDFrwOfMQD4wYqgJV/xQYqVvVqVvVqVvVqVvV', N'Nguyễn Văn A', N'user1@gmail.com', N'0912345678', N'User Address', 1, 1);

INSERT INTO categories (name, description) VALUES
    (N'Cơm', N'Các món cơm truyền thống'),
    (N'Phở - Bún', N'Phở, bún, hủ tiếu'),
    (N'Đồ uống', N'Nước giải khát, trà, cà phê'),
    (N'Tráng miệng', N'Bánh, chè, kem');

INSERT INTO foods (name, description, price, category_id, is_available) VALUES
    (N'Cơm sườn bì chả', N'Cơm trắng, sườn nướng, bì, chả', 35000, 1, 1),
    (N'Cơm gà xé', N'Cơm gà xé phay thơm ngon', 40000, 1, 1),
    (N'Phở bò đặc biệt', N'Phở bò tái, gầu, nạm', 45000, 2, 1),
    (N'Bún bò Huế', N'Bún bò Huế cay nồng', 40000, 2, 1),
    (N'Trà đá', N'Trà đá mát lạnh', 5000, 3, 1),
    (N'Cà phê đen đá', N'Cà phê đen đá nguyên chất', 15000, 3, 1),
    (N'Chè ba màu', N'Chè đậu đỏ, đậu xanh, thạch', 20000, 4, 1);

-- -----------------------------------------------------
-- 8. BẢNG food_images (Ảnh cho món ăn) - thêm để hỗ trợ nhiều ảnh
-- Quan hệ: Many food_images -> One food (ManyToOne với foods)
-- -----------------------------------------------------
CREATE TABLE food_images (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    food_id BIGINT NOT NULL,
    image_url NVARCHAR(1000) NOT NULL,
    is_main BIT NOT NULL DEFAULT 0,
    CONSTRAINT fk_food_images_food FOREIGN KEY (food_id) REFERENCES foods(id)
);

CREATE INDEX idx_food_images_food_id ON food_images(food_id);

PRINT N'Đã tạo xong các bảng và dữ liệu mẫu.';
PRINT N'Phương thức thanh toán: COD (id=1), MOMO (id=2)';
PRINT N'Bảng payment_transactions dùng để lưu thông tin giao dịch Momo (transaction_id, response code, etc).';

-- ALTER statements for adding profile location fields (apply to existing DB)
-- Run these if your database already exists and you need to update schema
IF COL_LENGTH('users', 'address_label') IS NULL
    ALTER TABLE users ADD address_label NVARCHAR(255);
IF COL_LENGTH('users', 'latitude') IS NULL
    ALTER TABLE users ADD latitude FLOAT NULL;
IF COL_LENGTH('users', 'longitude') IS NULL
    ALTER TABLE users ADD longitude FLOAT NULL;

--Tạo bảng reviews (Đánh giá món ăn)
CREATE TABLE dbo.reviews (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    food_id BIGINT NOT NULL,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment NVARCHAR(1000),
    created_at DATETIME DEFAULT GETDATE(),
    
    -- Khóa ngoại liên kết với bảng users
    CONSTRAINT fk_review_user FOREIGN KEY (user_id) 
        REFERENCES users (id) ON DELETE CASCADE,
        
    -- Khóa ngoại liên kết với bảng foods
    CONSTRAINT fk_review_food FOREIGN KEY (food_id) 
        REFERENCES foods (id) ON DELETE CASCADE
);
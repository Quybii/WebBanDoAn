IF OBJECT_ID('dbo.payment_transactions', 'U') IS NOT NULL DROP TABLE dbo.payment_transactions;
IF OBJECT_ID('dbo.cart_items', 'U') IS NOT NULL DROP TABLE dbo.cart_items;
IF OBJECT_ID('dbo.order_details', 'U') IS NOT NULL DROP TABLE dbo.order_details;
IF OBJECT_ID('dbo.orders', 'U') IS NOT NULL DROP TABLE dbo.orders;
IF OBJECT_ID('dbo.foods', 'U') IS NOT NULL DROP TABLE dbo.foods;
IF OBJECT_ID('dbo.categories', 'U') IS NOT NULL DROP TABLE dbo.categories;
IF OBJECT_ID('dbo.users', 'U') IS NOT NULL DROP TABLE dbo.users;
IF OBJECT_ID('dbo.roles', 'U') IS NOT NULL DROP TABLE dbo.roles;
IF OBJECT_ID('dbo.payment_methods', 'U') IS NOT NULL DROP TABLE dbo.payment_methods;

CREATE TABLE roles (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE payment_methods (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    code NVARCHAR(50) NOT NULL UNIQUE,
    name NVARCHAR(200) NOT NULL,
    description NVARCHAR(500),
    is_active BIT NOT NULL DEFAULT 1
);

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

CREATE TABLE categories (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(200) NOT NULL,
    description NVARCHAR(500),
    image_url NVARCHAR(500)
);

CREATE TABLE foods (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(200) NOT NULL,
    description NVARCHAR(1000),
    price DECIMAL(12,2) NOT NULL CHECK (price >= 0),
    image_url NVARCHAR(500),
    category_id BIGINT NOT NULL,
    is_available BIT NOT NULL DEFAULT 1,
    is_addon BIT NOT NULL DEFAULT 0,
    created_at DATETIME2 DEFAULT GETDATE(),
    CONSTRAINT fk_foods_category FOREIGN KEY (category_id) REFERENCES categories(id)
);

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

CREATE TABLE cart_items (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    food_id BIGINT NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0) DEFAULT 1,
    parent_cart_item_id BIGINT NULL,
    created_at DATETIME2 DEFAULT GETDATE(),
    CONSTRAINT fk_cart_items_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_cart_items_food FOREIGN KEY (food_id) REFERENCES foods(id),
    CONSTRAINT fk_cart_items_parent FOREIGN KEY (parent_cart_item_id) REFERENCES cart_items(id)
);


CREATE TABLE food_recommendations (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    food_id BIGINT NOT NULL,                    
    recommended_food_id BIGINT NOT NULL,        
    priority INT NOT NULL DEFAULT 1,             
    CONSTRAINT fk_food_rec_food FOREIGN KEY (food_id) REFERENCES foods(id),
    CONSTRAINT fk_food_rec_recommended FOREIGN KEY (recommended_food_id) REFERENCES foods(id),
    CONSTRAINT uq_food_recommendation UNIQUE (food_id, recommended_food_id)
);

CREATE INDEX idx_food_recommendations_food_id ON food_recommendations(food_id);

CREATE TABLE food_images (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    food_id BIGINT NOT NULL,
    image_url NVARCHAR(1000) NOT NULL,
    is_main BIT NOT NULL DEFAULT 0,
    CONSTRAINT fk_food_images_food FOREIGN KEY (food_id) REFERENCES foods(id)
);

CREATE INDEX idx_food_images_food_id ON food_images(food_id);

CREATE TABLE food_reviews (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    food_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment NVARCHAR(2000),
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2,
    CONSTRAINT fk_food_reviews_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_food_reviews_food FOREIGN KEY (food_id) REFERENCES foods(id),
    CONSTRAINT fk_food_reviews_order FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT uq_food_reviews_user_food UNIQUE (user_id, food_id)
);

CREATE INDEX idx_food_reviews_food_id ON food_reviews(food_id);
CREATE INDEX idx_food_reviews_user_id ON food_reviews(user_id);

-- Bảng food_review_images (Ảnh đánh giá)
CREATE TABLE food_review_images (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    review_id BIGINT NOT NULL,
    image_url NVARCHAR(1000) NOT NULL,
    CONSTRAINT fk_food_review_images_review FOREIGN KEY (review_id) REFERENCES food_reviews(id)
);

CREATE INDEX idx_food_review_images_review_id ON food_review_images(review_id);



-- BỨC 1: Tạo Roles (vai trò)
INSERT INTO roles (name) VALUES (N'USER'), (N'ADMIN');

-- BƯỚC 2: Tạo Payment Methods (phương thức thanh toán)
INSERT INTO payment_methods (code, name, description, is_active) VALUES
    (N'COD', N'Thanh toán khi nhận hàng', N'Thanh toán tiền mặt khi nhận hàng', 1),
    (N'MOMO', N'Chuyển khoản MoMo', N'Thanh toán qua ứng dụng MoMo', 1);

-- BƯỚC 3: Tạo Users (người dùng)
-- Password mẫu: 123456 (BCrypt hash - tạo bằng BCryptPasswordEncoder trong app nếu cần)
INSERT INTO users (username, password, full_name, email, phone, address, role_id, enabled)
VALUES 
    (N'admin', N'$2a$10$8K1p/a0dL1LXMIgoEDFrwOfMQD4wYqgJV/xQYqVvVqVvVqVvVqVvV', N'Quản trị viên', N'admin@webbandoan.com', N'0901234567', N'123 Đường Nguyễn Huệ, Q1, TP HCM', 2, 1),
    (N'user1', N'$2a$10$8K1p/a0dL1LXMIgoEDFrwOfMQD4wYqgJV/xQYqVvVqVvVqVvVqVvV', N'Nguyễn Văn A', N'user1@gmail.com', N'0912345678', N'456 Đường Lê Lợi, Q1, TP HCM', 1, 1),
    (N'user2', N'$2a$10$8K1p/a0dL1LXMIgoEDFrwOfMQD4wYqgJV/xQYqVvVqVvVqVvVqVvV', N'Trần Thị B', N'user2@gmail.com', N'0923456789', N'789 Đường Đinh Tiên Hoàng, Q1, TP HCM', 1, 1),
    (N'user3', N'$2a$10$8K1p/a0dL1LXMIgoEDFrwOfMQD4wYqgJV/xQYqVvVqVvVqVvVqVvV', N'Lê Văn C', N'user3@gmail.com', N'0934567890', N'321 Đường Ngô Đức Kế, Q1, TP HCM', 1, 1);

-- BƯỚC 4: Tạo Categories (danh mục)
INSERT INTO categories (name, description) VALUES
    (N'Cơm', N'Các món cơm truyền thống'),
    (N'Phở - Bún', N'Phở, bún, hủ tiếu'),
    (N'Đồ uống', N'Nước giải khát, trà, cà phê'),
    (N'Tráng miệng', N'Bánh, chè, kem'),
    (N'Thêm - Phụ trợ', N'Các mục thêm, phụ trợ');

-- BƯỚC 5: Tạo Foods (món ăn)
INSERT INTO foods (name, description, price, category_id, is_available) VALUES
    -- Danh mục Cơm
    (N'Cơm sườn bì chả', N'Cơm trắng, sườn nướng, bì, chả chuối', 35000, 1, 1),
    (N'Cơm gà xé', N'Cơm gà xé phay, cơm có dầu gà thơm', 40000, 1, 1),
    (N'Cơm thịt kho tàu', N'Cơm, thịt kho tàu, trứng cút', 38000, 1, 1),
    
    -- Danh mục Phở - Bún
    (N'Phở bò đặc biệt', N'Phở bò tái, gầu, nạm, hành, ngò', 45000, 2, 1),
    (N'Bún bò Huế', N'Bún bò Huế cay nồng, thịt nạc, mỡ', 40000, 2, 1),
    (N'Hủ tiếu nam vang', N'Hủ tiếu, tôm, giò heo, gan', 42000, 2, 1),
    
    -- Danh mục Đồ uống
    (N'Trà đá', N'Trà đen lạnh, nước cot chanh', 5000, 3, 1),
    (N'Cà phê đen đá', N'Cà phê đen đá nguyên chất', 15000, 3, 1),
    (N'Cà phê sữa nóng', N'Cà phê sữa nóng truyền thống', 18000, 3, 1),
    
    -- Danh mục Tráng miệng
    (N'Chè ba màu', N'Chè đậu đỏ, đậu xanh, thạch', 20000, 4, 1),
    (N'Kem trân châu', N'Kem mềm, trân châu, nước cam', 22000, 4, 1),
    
    -- Danh mục Thêm - Phụ trợ
    (N'Bún thêm', N'Đĩa bún thêm để ăn kèm', 8000, 5, 1),
    (N'Thịt thêm', N'Thêm thịt bò/gà/heo thêm', 12000, 5, 1),
    (N'Trứng cút', N'2 quả trứng cút nấu', 5000, 5, 1),
    (N'Nước ngọt', N'Coca, Sprite, Mirinda', 10000, 5, 1);

UPDATE foods
SET is_addon = 1
WHERE category_id = 5;

-- BƯỚC 6: Tạo Food Images (ảnh cho từng món ăn)
INSERT INTO food_images (food_id, image_url, is_main) VALUES
    (1, N'/uploads/foods/1/com-suon.jpg', 1),
    (2, N'/uploads/foods/2/com-ga.jpg', 1),
    (3, N'/uploads/foods/3/com-kho-tau.jpg', 1),
    (4, N'/uploads/foods/4/pho-bo.jpg', 1),
    (5, N'/uploads/foods/5/bun-bo-hue.jpg', 1),
    (6, N'/uploads/foods/6/hu-tieu.jpg', 1),
    (7, N'/uploads/foods/7/tra-da.jpg', 1),
    (8, N'/uploads/foods/8/ca-phe-da.jpg', 1),
    (9, N'/uploads/foods/9/ca-phe-sua.jpg', 1),
    (10, N'/uploads/foods/10/che-ba-mau.jpg', 1),
    (11, N'/uploads/foods/11/kem-tran-chau.jpg', 1),
    (12, N'/uploads/foods/12/bun-them.jpg', 1),
    (13, N'/uploads/foods/13/thit-them.jpg', 1),
    (14, N'/uploads/foods/14/trung-cut.jpg', 1),
    (15, N'/uploads/foods/15/nuoc-ngot.jpg', 1);

-- BƯỚC 7: Tạo Food Recommendations (gợi ý kèm theo)
-- Ví dụ: Khi chọn Cơm sườn (id=1) gợi ý thêm: Bún thêm, Thịt thêm, Nước ngọt
INSERT INTO food_recommendations (food_id, recommended_food_id, priority) VALUES
    -- Cơm sườn bì chả (id=1) -> gợi ý
    (1, 12, 1),  -- Cơm sườn + Bún thêm
    (1, 13, 2),  -- Cơm sườn + Thịt thêm
    (1, 15, 3),  -- Cơm sườn + Nước ngọt
    (1, 14, 4),  -- Cơm sườn + Trứng cút
    
    -- Cơm gà xé (id=2) -> gợi ý
    (2, 12, 1),  -- Cơm gà + Bún thêm
    (2, 13, 2),  -- Cơm gà + Thịt thêm
    (2, 7, 3),   -- Cơm gà + Trà đá
    (2, 8, 4),   -- Cơm gà + Cà phê
    
    -- Cơm thịt kho tàu (id=3) -> gợi ý
    (3, 12, 1),  -- Cơm kho tàu + Bún thêm
    (3, 13, 2),  -- Cơm kho tàu + Thịt thêm
    (3, 15, 3),  -- Cơm kho tàu + Nước ngọt
    
    -- Phở bò (id=4) -> gợi ý
    (4, 13, 1),  -- Phở bò + Thịt thêm
    (4, 7, 2),   -- Phở bò + Trà đá
    (4, 8, 3),   -- Phở bò + Cà phê
    
    -- Bún bò Huế (id=5) -> gợi ý
    (5, 12, 1),  -- Bún bò + Bún thêm
    (5, 13, 2),  -- Bún bò + Thịt thêm
    (5, 15, 3),  -- Bún bò + Nước ngọt
    
    -- Hủ tiếu (id=6) -> gợi ý
    (6, 13, 1),  -- Hủ tiếu + Thịt thêm
    (6, 7, 2),   -- Hủ tiếu + Trà đá
    (6, 15, 3);  -- Hủ tiếu + Nước ngọt

-- BƯỚC 8: Tạo Cart Items (giỏ hàng mẫu)
INSERT INTO cart_items (user_id, food_id, quantity) VALUES
    (2, 1, 1),   -- User1 có 1 cơm sườn
    (2, 7, 2),   -- User1 có 2 trà đá
    (3, 4, 1),   -- User2 có 1 phở bò
    (3, 8, 1);   -- User2 có 1 cà phê

-- BƯỚC 9: Tạo Orders (đơn hàng mẫu)
INSERT INTO orders (user_id, order_date, total_amount, status, shipping_address, phone, note, payment_method_id, payment_status) VALUES
    (2, GETDATE(), 100000, N'COMPLETED', N'456 Đường Lê Lợi, Q1, TP HCM', N'0912345678', N'Giao lúc 12h trưa', 1, N'COMPLETED'),
    (3, DATEADD(DAY, -1, GETDATE()), 85000, N'COMPLETED', N'789 Đường Đinh Tiên Hoàng, Q1, TP HCM', N'0923456789', N'Vui lòng giao nhanh', 1, N'COMPLETED'),
    (2, DATEADD(DAY, -2, GETDATE()), 125000, N'PENDING', N'456 Đường Lê Lợi, Q1, TP HCM', N'0912345678', N'Không cay', 2, N'PENDING'),
    (4, DATEADD(DAY, -3, GETDATE()), 75000, N'COMPLETED', N'321 Đường Ngô Đức Kế, Q1, TP HCM', N'0934567890', NULL, 1, N'COMPLETED');

-- BƯỚC 10: Tạo Order Details (chi tiết đơn hàng)
INSERT INTO order_details (order_id, food_id, quantity, unit_price, subtotal) VALUES
    -- Order 1 (id=1)
    (1, 1, 2, 35000, 70000),   -- 2x Cơm sườn
    (1, 7, 1, 5000, 5000),     -- 1x Trà đá
    (1, 12, 1, 8000, 8000),    -- 1x Bún thêm
    (1, 13, 1, 12000, 12000),  -- 1x Thịt thêm
    
    -- Order 2 (id=2)
    (2, 4, 1, 45000, 45000),   -- 1x Phở bò
    (2, 7, 2, 5000, 10000),    -- 2x Trà đá
    (2, 13, 1, 12000, 12000),  -- 1x Thịt thêm
    (2, 8, 1, 15000, 15000),   -- 1x Cà phê
    
    -- Order 3 (id=3)
    (3, 5, 1, 40000, 40000),   -- 1x Bún bò Huế
    (3, 12, 2, 8000, 16000),   -- 2x Bún thêm
    (3, 13, 1, 12000, 12000),  -- 1x Thịt thêm
    (3, 15, 3, 10000, 30000),  -- 3x Nước ngọt
    (3, 14, 1, 5000, 5000),    -- 1x Trứng cút
    
    -- Order 4 (id=4)
    (4, 2, 1, 40000, 40000),   -- 1x Cơm gà xé
    (4, 7, 2, 5000, 10000),    -- 2x Trà đá
    (4, 10, 1, 20000, 20000);  -- 1x Chè ba màu

-- BƯỚC 11: Tạo Payment Transactions (giao dịch thanh toán)
-- Chỉ tạo cho các giao dịch Momo (payment_method_id = 2)
INSERT INTO payment_transactions (order_id, momo_transaction_id, momo_request_id, order_info, amount, payment_status, created_date, response_code) VALUES
    (3, N'1234567890', N'REQ20260404001', N'Thanh toán Bún bò Huế', 125000, N'SUCCESS', DATEADD(DAY, -2, GETDATE()), 0);

--
-- ========== INDEX để tăng tốc độ truy vấn ==========
--
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

-- ALTER statements for adding profile location fields (apply to existing DB)
-- Run these if your database already exists and you need to update schema
IF COL_LENGTH('users', 'address_label') IS NULL
    ALTER TABLE users ADD address_label NVARCHAR(255);
IF COL_LENGTH('users', 'latitude') IS NULL
    ALTER TABLE users ADD latitude FLOAT NULL;
IF COL_LENGTH('users', 'longitude') IS NULL
    ALTER TABLE users ADD longitude FLOAT NULL;
IF COL_LENGTH('foods', 'is_addon') IS NULL
    ALTER TABLE foods ADD is_addon BIT NOT NULL CONSTRAINT df_foods_is_addon DEFAULT 0;

IF COL_LENGTH('cart_items', 'parent_cart_item_id') IS NULL
    ALTER TABLE cart_items ADD parent_cart_item_id BIGINT NULL;

IF NOT EXISTS (
    SELECT 1
    FROM sys.foreign_keys
    WHERE name = 'fk_cart_items_parent'
)
    ALTER TABLE cart_items
    ADD CONSTRAINT fk_cart_items_parent FOREIGN KEY (parent_cart_item_id) REFERENCES cart_items(id);

IF EXISTS (
    SELECT 1
    FROM sys.key_constraints
    WHERE name = 'uq_cart_user_food'
)
    ALTER TABLE cart_items DROP CONSTRAINT uq_cart_user_food;

IF OBJECT_ID('dbo.food_reviews', 'U') IS NULL
BEGIN
    CREATE TABLE food_reviews (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        user_id BIGINT NOT NULL,
        food_id BIGINT NOT NULL,
        order_id BIGINT NOT NULL,
        rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
        comment NVARCHAR(2000),
        created_at DATETIME2 DEFAULT GETDATE(),
        updated_at DATETIME2,
        CONSTRAINT fk_food_reviews_user FOREIGN KEY (user_id) REFERENCES users(id),
        CONSTRAINT fk_food_reviews_food FOREIGN KEY (food_id) REFERENCES foods(id),
        CONSTRAINT fk_food_reviews_order FOREIGN KEY (order_id) REFERENCES orders(id),
        CONSTRAINT uq_food_reviews_user_food UNIQUE (user_id, food_id)
    );
    CREATE INDEX idx_food_reviews_food_id ON food_reviews(food_id);
    CREATE INDEX idx_food_reviews_user_id ON food_reviews(user_id);
END

IF OBJECT_ID('dbo.food_review_images', 'U') IS NULL
BEGIN
    CREATE TABLE food_review_images (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        review_id BIGINT NOT NULL,
        image_url NVARCHAR(1000) NOT NULL,
        CONSTRAINT fk_food_review_images_review FOREIGN KEY (review_id) REFERENCES food_reviews(id)
    );
    CREATE INDEX idx_food_review_images_review_id ON food_review_images(review_id);
END



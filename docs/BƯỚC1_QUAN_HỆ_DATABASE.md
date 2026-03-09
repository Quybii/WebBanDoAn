# BƯỚC 1 – GIẢI THÍCH QUAN HỆ DATABASE

## 1. Sơ đồ quan hệ (ERD mô tả)

```
roles (1) -----< users (N)          One Role có nhiều User
categories (1) -----< foods (N)      One Category có nhiều Food
users (1) -----< orders (N)         One User có nhiều Order
orders (1) -----< order_details (N) One Order có nhiều OrderDetail
foods (1) -----< order_details (N)  One Food có nhiều OrderDetail (trong nhiều đơn)
users (1) -----< cart_items (N)     One User có nhiều CartItem
foods (1) -----< cart_items (N)     One Food có nhiều CartItem (của nhiều user)
```

## 2. ManyToOne (N – 1)

- **Ý nghĩa:** Nhiều bản ghi ở bảng “nhiều” trỏ về **một** bản ghi ở bảng “một”.
- **Ví dụ:**
  - **users → roles:** Nhiều user cùng một role (USER hoặc ADMIN) → `User` có `@ManyToOne Role`.
  - **foods → categories:** Nhiều món cùng một danh mục → `Food` có `@ManyToOne Category`.
  - **orders → users:** Nhiều đơn hàng của một user → `Order` có `@ManyToOne User`.
  - **order_details → orders:** Nhiều dòng chi tiết thuộc một đơn → `OrderDetail` có `@ManyToOne Order`.
  - **order_details → foods:** Nhiều dòng chi tiết có thể cùng loại món (trong các đơn khác nhau) → `OrderDetail` có `@ManyToOne Food`.
  - **cart_items → users, foods:** Mỗi dòng giỏ thuộc một user và một món → `CartItem` có `@ManyToOne User` và `@ManyToOne Food`.

Trong Java: bên “nhiều” giữ **reference** tới entity “một” (ví dụ `private Role role;`).

## 3. OneToMany (1 – N)

- **Ý nghĩa:** Một bản ghi ở bảng “một” có **nhiều** bản ghi liên quan ở bảng “nhiều”.
- **Ví dụ:**
  - **roles → users:** Một role có nhiều user → `Role` có `@OneToMany List<User> users`.
  - **categories → foods:** Một danh mục có nhiều món → `Category` có `@OneToMany List<Food> foods`.
  - **orders → order_details:** Một đơn có nhiều dòng chi tiết → `Order` có `@OneToMany List<OrderDetail> orderDetails`.
  - **users → orders:** Một user có nhiều đơn → `User` có `@OneToMany List<Order> orders`.
  - **users → cart_items:** Một user có nhiều dòng giỏ → `User` có `@OneToMany List<CartItem> cartItems`.

Trong Java: bên “một” giữ **collection** (List/Set) của entity “nhiều”. Có thể dùng `mappedBy` trỏ về thuộc tính ManyToOne bên kia để tránh bảng trung gian thừa.

## 4. Ánh xạ với script SQL

| Bảng          | Khóa chính | Khóa ngoại        | Ghi chú                          |
|---------------|------------|--------------------|----------------------------------|
| roles         | id         | -                  | USER, ADMIN                      |
| users         | id         | role_id → roles    | ManyToOne Role                   |
| categories    | id         | -                  | Danh mục món                     |
| foods         | id         | category_id → categories | ManyToOne Category        |
| orders        | id         | user_id → users    | ManyToOne User                   |
| order_details | id         | order_id, food_id  | ManyToOne Order, ManyToOne Food  |
| cart_items    | id         | user_id, food_id   | ManyToOne User, ManyToOne Food; UNIQUE(user_id, food_id) |

File script tạo bảng và seed data: **`sql/schema.sql`** (chạy trên SQL Server).

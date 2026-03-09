# BƯỚC 10 – ADMIN DASHBOARD

## Đã hoàn thành

### 1. Service (bổ sung method cho admin)

- **FoodService.java** – thêm:
  - `findAll()` – tất cả món (không lọc is_available).
  - `save(Food)` – lưu món (tạo mới hoặc cập nhật).
  - `deleteById(Long)` – xóa món.

- **OrderService.java** – thêm:
  - `findAll()` – tất cả đơn hàng.
  - `findById(Long)` – lấy đơn theo id (không kiểm tra user).
  - `updateStatus(Long, String)` – cập nhật trạng thái đơn (PENDING, CONFIRMED, DELIVERING, COMPLETED, CANCELLED).

- **CategoryService.java** – thêm `findById(Long)` để load Category khi admin tạo/sửa món.

### 2. Controller

- **AdminDashboardController.java**
  - **GET /admin** – dashboard: thống kê tổng số món, tổng số đơn, đơn chờ xử lý.

- **AdminFoodController.java** (CRUD món ăn)
  - **GET /admin/foods** – danh sách tất cả món (bảng).
  - **GET /admin/foods/new** – form tạo món mới.
  - **GET /admin/foods/{id}/edit** – form sửa món.
  - **POST /admin/foods** – lưu món (tạo mới hoặc cập nhật), validate (tên, giá >= 0, category), redirect `/admin/foods` + flash message.
  - **POST /admin/foods/{id}/delete** – xóa món, redirect `/admin/foods` + flash message.

- **AdminOrderController.java** (quản lý đơn hàng)
  - **GET /admin/orders** – danh sách tất cả đơn hàng (bảng).
  - **GET /admin/orders/{id}** – chi tiết đơn hàng (thông tin khách, địa chỉ, bảng OrderDetail, form cập nhật trạng thái).
  - **POST /admin/orders/{id}/status** – cập nhật trạng thái đơn (PENDING/CONFIRMED/DELIVERING/COMPLETED/CANCELLED), redirect `/admin/orders/{id}` + flash message.

### 3. Layout

- **layout/admin-header.html** – fragment header riêng cho admin:
  - Logo "Admin Panel", nav (Dashboard, Quản lý món, Quản lý đơn hàng, Về trang chủ), form logout.

### 4. Template

- **admin/dashboard.html** – dashboard: 3 stat card (tổng món, tổng đơn, đơn chờ), nút hành động (Thêm món, Quản lý món, Quản lý đơn).

- **admin/food-list.html** – bảng danh sách món: ID, tên, danh mục, giá, trạng thái (Đang bán/Ngừng bán), nút Sửa/Xóa. Nút "Thêm món mới" ở header.

- **admin/food-form.html** – form tạo/sửa món: tên, mô tả, giá, danh mục (dropdown), URL ảnh, checkbox "Đang bán", nút Lưu/Hủy. Dùng chung cho create và edit (dựa vào `food.id`).

- **admin/order-list.html** – bảng danh sách đơn: ID, ngày đặt, khách hàng (username), tổng tiền, trạng thái (badge màu), nút "Xem chi tiết".

- **admin/order-detail.html** – chi tiết đơn: thông tin khách (username, ngày đặt, địa chỉ, SĐT, ghi chú), section cập nhật trạng thái (dropdown + nút), bảng OrderDetail (món, đơn giá, số lượng, thành tiền), tổng cộng, nút "Quay lại danh sách".

### 5. CSS

- **admin.css** – style cho admin:
  - `.admin-header`, `.admin-header-inner`, `.admin-logo`, `.admin-nav` – header admin (nền đen #1a1a2e).
  - `.admin-main` – main content.
  - `.admin-page-title`, `.admin-page-header` – tiêu đề và header trang.
  - `.admin-message` (success/error) – thông báo.
  - `.admin-stats`, `.stat-card`, `.stat-value` – dashboard stats.
  - `.admin-table-container`, `.admin-table` – bảng admin.
  - `.admin-form-container` – container form.
  - `.order-status-section`, `.status-form`, `.status-select` – form cập nhật trạng thái đơn.
  - Responsive cho mobile.

### 6. Security

- **SecurityConfig.java** – đã có sẵn: `/admin/**` yêu cầu `hasRole("ADMIN")`. Chỉ user có role ADMIN mới truy cập được các trang admin.

---

## Phân quyền ADMIN

- URL `/admin/**` yêu cầu role **ADMIN** (tức authority `ROLE_ADMIN`).
- User thường (role USER) không thể truy cập `/admin/**` → sẽ bị chặn 403 hoặc redirect.
- Để có tài khoản ADMIN: cập nhật trong DB (`users.role_id = 2` tương ứng `roles.id = 2` với `roles.name = 'ADMIN'`) hoặc tạo user mới với role ADMIN.

---

## CRUD món ăn

1. **Create:** GET `/admin/foods/new` → form → POST `/admin/foods` → lưu → redirect `/admin/foods`.
2. **Read:** GET `/admin/foods` → danh sách tất cả món.
3. **Update:** GET `/admin/foods/{id}/edit` → form (pre-fill) → POST `/admin/foods` → cập nhật → redirect `/admin/foods`.
4. **Delete:** POST `/admin/foods/{id}/delete` → xóa → redirect `/admin/foods`.

---

## Quản lý đơn hàng

1. **Danh sách đơn:** GET `/admin/orders` → bảng tất cả đơn (sắp theo ngày).
2. **Chi tiết đơn:** GET `/admin/orders/{id}` → thông tin đơn + bảng OrderDetail + form cập nhật trạng thái.
3. **Cập nhật trạng thái:** POST `/admin/orders/{id}/status` với `status` (PENDING/CONFIRMED/DELIVERING/COMPLETED/CANCELLED) → cập nhật → redirect `/admin/orders/{id}`.

---

## Lưu ý

- Admin có thể xem và quản lý tất cả đơn hàng (không chỉ của mình).
- Admin có thể xóa món ăn (cần cẩn thận, có thể ảnh hưởng đến đơn hàng đã đặt).
- Trạng thái đơn: PENDING (mặc định khi đặt) → CONFIRMED → DELIVERING → COMPLETED, hoặc CANCELLED.

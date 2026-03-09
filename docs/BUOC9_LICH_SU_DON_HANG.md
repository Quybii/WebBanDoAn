# BƯỚC 9 – LỊCH SỬ ĐƠN HÀNG

## Đã hoàn thành

### 1. Controller

- **OrderController.java** – thêm 2 method:
  - **GET /orders** (`orderHistory(Model model)`): Lấy danh sách đơn của user hiện tại qua `OrderService.findByUser(user)`, đưa vào model, trả view `"order-history"`.
  - **GET /orders/{id}** (`orderDetail(@PathVariable Long id, Model model)`): Lấy đơn theo id (chỉ khi thuộc user) qua `OrderService.findByIdAndUser(id, user)`, đưa vào model, trả view `"order-detail"`. Nếu không tìm thấy hoặc không thuộc user → redirect `/orders`.

### 2. Service

- **OrderService.java** – đã có sẵn từ Bước 8:
  - `findByUser(User user)` – danh sách đơn của user, sắp theo ngày giảm dần.
  - `findByIdAndUser(Long orderId, User user)` – lấy đơn theo id, chỉ khi thuộc user.

### 3. Template

- **order-history.html**
  - Danh sách đơn hàng: mỗi đơn hiển thị trong card (mã đơn, ngày đặt, trạng thái với badge màu, tổng tiền, địa chỉ, SĐT).
  - Badge trạng thái: PENDING (vàng), CONFIRMED (xanh nhạt), DELIVERING (xanh), COMPLETED (xanh lá), CANCELLED (đỏ).
  - Nút "Xem chi tiết" → `/orders/{id}`.
  - Trường hợp chưa có đơn: thông báo + link "Xem danh sách món".

- **order-detail.html**
  - Breadcrumb: Trang chủ / Lịch sử đơn hàng / Đơn hàng #{id}.
  - Header: mã đơn + badge trạng thái.
  - Thông tin đơn: ngày đặt, địa chỉ giao hàng, SĐT, ghi chú (nếu có).
  - Bảng chi tiết món: tên món, đơn giá, số lượng, thành tiền (từng dòng OrderDetail).
  - Tổng cộng ở cuối bảng.
  - Nút "Quay lại lịch sử" → `/orders`, "Tiếp tục mua sắm" → `/foods`.

### 4. CSS

- **cart.css** – thêm:
  - `.order-list`, `.order-card` – danh sách đơn dạng card.
  - `.order-card-header`, `.order-card-info`, `.order-card-id`, `.order-card-date` – header card.
  - `.status-badge`, `.status-pending`, `.status-confirmed`, `.status-delivering`, `.status-completed`, `.status-cancelled` – badge trạng thái với màu.
  - `.order-card-body`, `.order-card-total`, `.order-card-address`, `.order-card-phone` – nội dung card.
  - `.order-empty` – trường hợp chưa có đơn.
  - `.order-detail-box`, `.order-detail-header`, `.order-detail-title` – trang chi tiết.
  - `.order-info-section`, `.order-items-section` – section thông tin và chi tiết món.
  - `.order-detail-table` – bảng chi tiết món (tương tự cart-table).
  - `.order-total-label`, `.order-total-amount` – tổng cộng trong bảng.
  - `.order-detail-actions` – nút hành động.
  - Responsive cho mobile.

---

## Luồng xử lý

1. **Xem lịch sử:** User vào `/orders` → OrderController.orderHistory → OrderService.findByUser → hiển thị danh sách đơn (sắp mới nhất trước).
2. **Xem chi tiết:** User click "Xem chi tiết" trên một đơn → GET `/orders/{id}` → OrderController.orderDetail → OrderService.findByIdAndUser → hiển thị chi tiết đơn + bảng OrderDetail.

---

## Lưu ý

- Chỉ user đã đăng nhập mới xem được lịch sử đơn của mình (SecurityConfig: `/orders/**` authenticated).
- User chỉ xem được đơn của chính mình (kiểm tra trong `findByIdAndUser`).
- Trạng thái đơn hiện tại là PENDING (Bước 10 Admin sẽ có chức năng cập nhật trạng thái).

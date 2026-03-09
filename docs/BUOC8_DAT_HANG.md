# BƯỚC 8 – ĐẶT HÀNG

## Đã hoàn thành

### 1. Repository

- **OrderRepository.java** – `findByUserOrderByOrderDateDesc(User user)` (dùng cho lịch sử đơn ở Bước 9).
- **CartItemRepository.java** – thêm `deleteByUser(User user)` (@Modifying @Query) để xóa toàn bộ giỏ của user sau khi đặt hàng.

### 2. Service

- **CartService.java** – thêm **clearCart(User user)** gọi `cartItemRepository.deleteByUser(user)`.
- **OrderService.java**
  - **placeOrder(user, shippingAddress, phone, note):**
    1. Lấy giỏ của user (CartService.getCartItems).
    2. Nếu giỏ trống → return null.
    3. Tạo Order (user, totalAmount, status PENDING, shippingAddress, phone, note).
    4. Với mỗi CartItem: tạo OrderDetail(order, food, quantity, food.getPrice()), add vào order.getOrderDetails().
    5. orderRepository.save(order) (cascade lưu OrderDetail).
    6. cartService.clearCart(user).
  - Toàn bộ trong **@Transactional**: lỗi giữa chừng → rollback (không mất giỏ, không tạo đơn dở).
  - **findByIdAndUser(orderId, user)** – lấy đơn theo id, chỉ khi thuộc user (dùng cho order-success và Bước 9).
  - **findByUser(user)** – danh sách đơn của user (Bước 9).

### 3. Controller

- **OrderController.java** (user hiện tại từ SecurityContext + UserRepository)
  - **GET /checkout** – hiển thị form thanh toán: cartItems, totalAmount, user (pre-fill địa chỉ, SĐT). Nếu giỏ trống → redirect /cart.
  - **POST /checkout** – nhận shippingAddress, phone, note; validate; gọi OrderService.placeOrder; redirect /order-success?orderId=... hoặc redirect /checkout (lỗi) / /cart (giỏ trống).
  - **GET /order-success?orderId=...** – hiển thị trang cảm ơn, mã đơn, tổng tiền; chỉ khi order thuộc user hiện tại.

### 4. Template

- **checkout.html**
  - Form POST /checkout: địa chỉ giao hàng, SĐT, ghi chú (pre-fill từ user.address, user.phone).
  - Khối tóm tắt đơn: danh sách món (tên, số lượng x đơn giá), tổng cộng.
  - Nút "Xác nhận đặt hàng", link "Quay lại giỏ hàng". CSRF token trong form.
- **order-success.html**
  - Thông báo đặt hàng thành công, mã đơn (order.id), tổng tiền.
  - Link "Xem đơn hàng của tôi" → /orders (Bước 9), "Về trang chủ" → /.

### 5. CSS

- **cart.css** – thêm: checkout-layout (grid 2 cột), checkout-form-section, checkout-summary, checkout-items, checkout-item, checkout-total; order-success-box, order-success-title, order-success-actions; responsive.

---

## @Transactional

- **OrderService.placeOrder** được đánh dấu **@Transactional**.
- Nếu bất kỳ bước nào (save order, clear cart) lỗi → toàn bộ transaction rollback:
  - Không tạo Order/OrderDetail dở.
  - Giỏ hàng không bị xóa.
- Đảm bảo dữ liệu nhất quán: hoặc đặt hàng thành công (đơn + xóa giỏ), hoặc không thay đổi gì.

---

## Luồng đặt hàng

1. User vào giỏ (/cart) → bấm "Đặt hàng" → GET /checkout.
2. Trang checkout: form địa chỉ, SĐT, ghi chú + tóm tắt giỏ.
3. User điền form, bấm "Xác nhận đặt hàng" → POST /checkout.
4. OrderController validate → OrderService.placeOrder(user, ...).
5. OrderService: tạo Order + OrderDetail từ giỏ → save order → clearCart(user).
6. Redirect /order-success?orderId=...
7. Trang order-success hiển thị mã đơn, tổng tiền, link "Xem đơn hàng" (/orders) và "Về trang chủ".

---

## Lưu ý

- Link "Xem đơn hàng của tôi" (/orders) sẽ có ở Bước 9 (lịch sử đơn).
- Giỏ trống khi vào /checkout → redirect /cart; khi POST /checkout với giỏ đã xóa (race) → redirect /cart với flash message.

# BƯỚC 7 – GIỎ HÀNG

## Đã hoàn thành

### 1. Repository

- **CartItemRepository.java**
  - `findByUserOrderByCreatedAtDesc(User user)` – lấy tất cả dòng giỏ của user, sắp mới nhất.
  - `findByUserAndFood(User user, Food food)` – tìm dòng (user + món) để thêm số lượng hoặc tạo mới.

### 2. Service

- **CartService.java**
  - **addItem(user, foodId, quantity):** Nếu đã có (user, food) thì cộng thêm quantity; chưa có thì tạo CartItem mới. Chỉ thêm khi món tồn tại và đang bán.
  - **removeItem(user, cartItemId):** Xóa dòng chỉ khi thuộc user hiện tại.
  - **updateQuantity(user, cartItemId, quantity):** Cập nhật số lượng; nếu quantity ≤ 0 thì xóa dòng.
  - **getCartItems(user):** Danh sách CartItem của user (có Food).
  - **getTotalAmount(user):** Tổng tiền = Σ (đơn giá × số lượng).

### 3. Controller

- **CartController.java** (user hiện tại lấy từ SecurityContext + UserRepository)
  - **GET /cart** – xem giỏ: cartItems, totalAmount → view `cart`.
  - **GET /cart/add/{foodId}** – thêm món (mặc định quantity=1), redirect về `/foods/{foodId}` hoặc `?redirectTo=cart` → `/cart`.
  - **POST /cart/update** – cập nhật số lượng (cartItemId, quantity), redirect `/cart` + flash success.
  - **POST /cart/remove/{id}** – xóa dòng, redirect `/cart` + flash success.

### 4. Entity

- **CartItem.java** – thêm **getSubtotal()** = price × quantity (BigDecimal) để hiển thị thành tiền từng dòng.

### 5. Template

- **cart.html**
  - Bảng: Món ăn (link chi tiết), Đơn giá, Số lượng (input + form Cập nhật), Thành tiền (item.subtotal), Nút Xóa (form POST /cart/remove/{id}, confirm).
  - Tổng cộng (totalAmount).
  - Nút "Tiếp tục mua" → /foods, "Đặt hàng" → /checkout (Bước 8).
  - Giỏ trống: thông báo + link "Xem danh sách món".
  - Form update/remove có CSRF token.

### 6. CSS

- **cart.css** – style bảng giỏ, form cập nhật, nút xóa, tổng cộng, giỏ trống, responsive.

---

## Luồng chức năng

1. **Thêm món:** Trang chi tiết món (hoặc danh sách) bấm "Thêm vào giỏ" → GET /cart/add/{foodId} → CartService.addItem → redirect về trang món hoặc /cart.
2. **Xem giỏ:** GET /cart → CartService.getCartItems + getTotalAmount → hiển thị bảng + tổng.
3. **Cập nhật số lượng:** Trong giỏ, sửa ô số lượng, bấm "Cập nhật" → POST /cart/update → CartService.updateQuantity → redirect /cart.
4. **Xóa dòng:** Bấm "Xóa" → confirm → POST /cart/remove/{id} → CartService.removeItem → redirect /cart.

---

## Lưu ý

- Chỉ user đã đăng nhập mới vào được /cart (SecurityConfig: /cart/** authenticated).
- "Đặt hàng" dẫn đến /checkout – Bước 8 sẽ tạo trang checkout và lưu Order.
- Không dùng cart.js: update/remove dùng form submit (server-side), đơn giản cho đồ án.

# BƯỚC 3 – TRANG CHỦ – GIẢI THÍCH TỪNG FILE

## 1. Controller

### **HomeController.java**
- **Vai trò:** Xử lý request GET `/` (trang chủ).
- **Luồng:** Gọi `CategoryService.findAll()` lấy danh mục, `FoodService.findFeatured()` lấy món nổi bật (tối đa 8 món đang bán). Đưa `categories` và `featuredFoods` vào `Model`, trả về tên view `"home"`.
- **File dùng để:** Map URL `/` → logic lấy dữ liệu → trả view.

---

## 2. Service

### **CategoryService.java**
- **Vai trò:** Logic nghiệp vụ liên quan danh mục (trang chủ, sau này lọc món).
- **Method:** `findAll()` – lấy tất cả danh mục, sắp theo tên, gọi `CategoryRepository.findAllByOrderByNameAsc()`.
- **File dùng để:** Tách logic “lấy danh mục” ra khỏi Controller, dùng lại nhiều nơi.

### **FoodService.java**
- **Vai trò:** Logic nghiệp vụ liên quan món ăn (trang chủ, danh sách, chi tiết, tìm kiếm).
- **Method:** `findFeatured()` – lấy tối đa 8 món đang bán, sắp mới nhất, gọi `FoodRepository.findTop8ByIsAvailableTrueOrderByIdDesc()`.
- **File dùng để:** Cung cấp “món nổi bật” cho trang chủ và chuẩn bị cho phân trang/tìm kiếm (Bước 4).

---

## 3. Repository

### **CategoryRepository.java**
- **Vai trò:** Truy vấn bảng `categories`.
- **Method:** `findAllByOrderByNameAsc()` – trả về danh sách danh mục sắp theo tên (Spring Data JPA tự sinh câu lệnh từ tên method).
- **File dùng để:** Giao tiếp database cho danh mục.

### **FoodRepository.java**
- **Vai trò:** Truy vấn bảng `foods`.
- **Method:** `findTop8ByIsAvailableTrueOrderByIdDesc()` – lấy tối đa 8 món đang bán, sắp id giảm dần (món mới trước). Các method khác dùng cho Bước 4 (phân trang, lọc danh mục, tìm theo tên).
- **File dùng để:** Giao tiếp database cho món ăn.

---

## 4. Config

### **SecurityConfig.java**
- **Vai trò:** Cấu hình Spring Security tạm thời cho Bước 3: cho phép tất cả request (permitAll), tắt form login và CSRF để xem trang chủ không cần đăng nhập.
- **File dùng để:** Tránh bị chặn 401 khi mở `/`; Bước 6 sẽ bổ sung form login và phân quyền USER/ADMIN.

---

## 5. Templates (Thymeleaf)

### **layout/header.html**
- **Vai trò:** Định nghĩa fragment `header` – thanh navigation chung (logo, Trang chủ, Danh sách món, Giỏ hàng, Đăng nhập).
- **File dùng để:** Mọi trang dùng `th:replace="~{layout/header :: header}"` để có cùng header, tránh lặp code.

### **layout/footer.html**
- **Vai trò:** Định nghĩa fragment `footer` – bản quyền, link Trang chủ / Danh sách món, và script chung `main.js`.
- **File dùng để:** Footer thống nhất và load script chung một chỗ.

### **home.html**
- **Vai trò:** View trang chủ. Dùng `th:replace` nhúng header và footer, hiển thị:
  - Hero: tiêu đề + mô tả ngắn.
  - Danh mục: danh sách category (link sang `/foods?categoryId=...` – Bước 4 sẽ xử lý).
  - Món nổi bật: lưới thẻ món (ảnh, tên, mô tả rút gọn, giá, nút “Xem chi tiết” → `/foods/{id}`).
- **File dùng để:** Trang chủ hiển thị danh mục và món nổi bật từ model do `HomeController` đưa vào.

---

## 6. CSS

### **static/css/style.css**
- **Vai trò:** CSS dùng chung: biến màu (primary, secondary, bg), container, header (logo + nav), footer, nút (.btn, .btn-primary, .btn-add-cart), section-title, empty-msg.
- **File dùng để:** Giao diện thống nhất toàn site; màu thương hiệu (cam #c45c26, xanh đậm #2c3e50).

### **static/css/home.css**
- **Vai trò:** CSS riêng trang chủ: hero (banner gradient), danh mục (category cards), món nổi bật (food grid, card ảnh/tên/giá/nút).
- **File dùng để:** Chỉ trang chủ (`home.html`) load thêm file này để layout và style riêng không trộn vào style chung.

---

## 7. JS

### **static/js/main.js**
- **Vai trò:** Script chung (hiện tại placeholder). Được load trong footer.
- **File dùng để:** Sau này thêm menu mobile, xử lý chung; trang nào cần script riêng có thể thêm file riêng (Bước 4, 7, …).

---

## Luồng khi truy cập `/`

1. Browser gửi GET `/`.
2. `HomeController.home(Model)` được gọi.
3. Controller gọi `CategoryService.findAll()` → Repository → trả `List<Category>`.
4. Controller gọi `FoodService.findFeatured()` → Repository → trả `List<Food>` (tối đa 8).
5. Controller đưa `categories` và `featuredFoods` vào Model, return `"home"`.
6. Thymeleaf render `templates/home.html` với model, nhúng `layout/header` và `layout/footer`.
7. Trình duyệt nhận HTML, tải thêm `/css/style.css`, `/css/home.css`, `/js/main.js` từ static.

---

## Lưu ý

- Link “Danh sách món” (`/foods`), “Giỏ hàng” (`/cart`), “Đăng nhập” (`/login`) chưa có controller tương ứng → sẽ 404 cho đến khi làm Bước 4, 6, 7.
- “Xem chi tiết” trên thẻ món dẫn đến `/foods/{id}` – Bước 5 sẽ có trang chi tiết.

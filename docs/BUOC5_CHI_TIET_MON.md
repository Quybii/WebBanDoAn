# BƯỚC 5 – TRANG CHI TIẾT MÓN

## Đã hoàn thành

### 1. Controller

- **FoodController.java** – Thêm method:
  - **GET /foods/{id}** (`detail(Long id, Model model)`):
    - Gọi `FoodService.findById(id)`.
    - Nếu không tìm thấy món → `redirect:/foods`.
    - Nếu có → đưa `food` vào Model, trả về view `"food-detail"`.

### 2. Template

- **food-detail.html**
  - Breadcrumb: Trang chủ / Danh sách món / Tên món.
  - Layout 2 cột: ảnh món (trái) + thông tin (phải).
  - Thông tin: tên, danh mục, giá, mô tả (nếu có).
  - Nút **Thêm vào giỏ** → link `/cart/add/{id}` (Bước 7 sẽ xử lý).
  - Nút **Tiếp tục mua sắm** → link `/foods`.

### 3. CSS

- **food.css** – Thêm section cho trang chi tiết:
  - `.breadcrumb` – đường dẫn.
  - `.food-detail` – grid 2 cột (ảnh | info).
  - `.food-detail-image`, `.food-detail-placeholder` – ảnh hoặc placeholder.
  - `.food-detail-info`, `.food-detail-title`, `.food-detail-category`, `.food-detail-price`, `.food-detail-desc` – nội dung.
  - `.food-detail-actions` – vùng nút (Thêm vào giỏ, Tiếp tục mua sắm).
  - `.btn-add-cart-large` – nút thêm giỏ lớn.
  - Responsive: màn nhỏ chuyển 1 cột, ảnh giới hạn chiều cao.

---

## Luồng xử lý

1. User click "Xem chi tiết" trên danh sách món hoặc trang chủ → GET `/foods/{id}`.
2. `FoodController.detail(id, model)` được gọi.
3. Service `findById(id)` → Repository → trả về `Food` hoặc `null`.
4. Nếu `null` → redirect `/foods`.
5. Nếu có → `model.addAttribute("food", food)`, return `"food-detail"`.
6. Thymeleaf render `food-detail.html` với `food` (tên, giá, danh mục, mô tả, ảnh).

---

## Lưu ý

- **Thêm vào giỏ:** Link `/cart/add/{id}` chưa có controller → sẽ 404 cho đến khi làm Bước 7 (CartController, CartService). Sau Bước 7, nút sẽ hoạt động.
- Trang chi tiết dùng chung **food.css** với trang danh sách; phần detail nằm trong cùng file CSS.

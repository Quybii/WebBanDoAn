# BƯỚC 4 – TÓM TẮT: TRANG DANH SÁCH MÓN

## Đã hoàn thành

### 1. Controller

- **FoodController.java**
  - GET `/foods` với query params: `page`, `size`, `categoryId`, `keyword`.
  - Logic: ưu tiên tìm kiếm keyword → lọc category → tất cả món.
  - Tạo `Pageable` từ `page` và `size`, gọi Service, đưa `Page<Food>` vào Model.

### 2. Template

- **food-list.html**
  - Form lọc: dropdown danh mục, ô tìm kiếm, nút "Tìm kiếm", "Xóa bộ lọc".
  - Grid hiển thị món (tái dùng style từ home.css).
  - UI phân trang: Previous, số trang hiện tại/tổng, Next (giữ nguyên filter khi chuyển trang).

### 3. CSS

- **food.css**
  - Form lọc (filter-section, filter-row, filter-select, filter-input).
  - Thông báo kết quả (result-info).
  - Grid món (food-card, food-card-image, food-card-body).
  - Phân trang (pagination, pagination-btn, pagination-info).
  - Responsive cho mobile.

### 4. Tài liệu

- **docs/BUOC4_DANH_SACH_MON.md** – Giải thích chi tiết Pageable, cách dùng trong Controller/Repository/Service, ví dụ thực tế.

---

## Chức năng

- **Phân trang:** Mặc định 12 món/trang, có thể đổi qua URL (`?size=20`).
- **Lọc theo danh mục:** Dropdown chọn category → chỉ hiển thị món thuộc category đó.
- **Tìm kiếm:** Nhập keyword → tìm món có tên chứa keyword (không phân biệt hoa thường).
- **Kết hợp:** Có thể lọc category + tìm kiếm cùng lúc (ưu tiên tìm kiếm).

---

## Luồng xử lý

1. User truy cập `/foods` hoặc `/foods?categoryId=1&page=0`.
2. `FoodController.list()` nhận params, tạo `Pageable`.
3. Gọi `FoodService` (tùy có keyword/categoryId hay không).
4. Service gọi `FoodRepository` với `Pageable`.
5. Repository trả `Page<Food>` (chứa 12 món + metadata).
6. Controller đưa vào Model, render `food-list.html`.
7. Thymeleaf hiển thị form lọc, grid món, phân trang.

---

## Lưu ý

- Link "Xem chi tiết" trên thẻ món dẫn đến `/foods/{id}` – Bước 5 sẽ tạo trang chi tiết.
- Nút "Thêm vào giỏ" chưa có (sẽ làm ở Bước 7 – Giỏ hàng).
- Phân trang giữ nguyên filter khi chuyển trang (categoryId, keyword được giữ trong URL).

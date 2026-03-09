# BƯỚC 4 – TRANG DANH SÁCH MÓN – GIẢI THÍCH PAGEABLE

## 1. Pageable là gì?

**Pageable** là interface của Spring Data JPA dùng để **phân trang** (pagination). Thay vì load tất cả dữ liệu một lúc, ta chỉ load một phần (ví dụ: 12 món mỗi trang).

### Lợi ích:
- **Hiệu năng:** Không load hàng nghìn bản ghi cùng lúc → nhanh hơn.
- **Trải nghiệm:** Người dùng dễ xem, có nút "Trang trước / Trang sau".

---

## 2. Cách tạo Pageable trong Controller

```java
@GetMapping("/foods")
public String list(
        @RequestParam(defaultValue = "0") int page,    // Số trang (bắt đầu từ 0)
        @RequestParam(defaultValue = "12") int size,    // Số món mỗi trang
        Model model) {
    
    // Tạo Pageable từ tham số URL
    Pageable pageable = PageRequest.of(page, size);
    
    // Gọi Service với Pageable
    Page<Food> foodPage = foodService.findAllAvailable(pageable);
    
    // Đưa vào Model
    model.addAttribute("foodPage", foodPage);
    model.addAttribute("foods", foodPage.getContent()); // List<Food> của trang hiện tại
    model.addAttribute("currentPage", page);
    model.addAttribute("totalPages", foodPage.getTotalPages());
    
    return "food-list";
}
```

**Giải thích:**
- `PageRequest.of(page, size)` tạo đối tượng Pageable.
- `page`: số trang (0 = trang đầu, 1 = trang 2, ...).
- `size`: số phần tử mỗi trang (ví dụ: 12 món).

---

## 3. Repository method với Pageable

```java
@Repository
public interface FoodRepository extends JpaRepository<Food, Long> {
    
    // Method trả về Page<Food> thay vì List<Food>
    Page<Food> findByIsAvailableTrueOrderByIdDesc(Pageable pageable);
}
```

**Lưu ý:**
- Tham số cuối cùng phải là `Pageable`.
- Kiểu trả về là `Page<Food>` (không phải `List<Food>`).

---

## 4. Service method với Pageable

```java
@Service
public class FoodService {
    
    @Transactional(readOnly = true)
    public Page<Food> findAllAvailable(Pageable pageable) {
        return foodRepository.findByIsAvailableTrueOrderByIdDesc(pageable);
    }
}
```

Service chỉ truyền `Pageable` xuống Repository, không cần xử lý gì thêm.

---

## 5. Đối tượng Page<Food>

Khi Repository trả về `Page<Food>`, ta có các thông tin:

| Thuộc tính / Method | Ý nghĩa |
|---------------------|---------|
| `getContent()` | `List<Food>` – danh sách món của trang hiện tại |
| `getTotalPages()` | Tổng số trang |
| `getTotalElements()` | Tổng số món (tất cả trang) |
| `getNumber()` | Số trang hiện tại (0-based) |
| `getSize()` | Số món mỗi trang |
| `isFirst()` | Có phải trang đầu? |
| `isLast()` | Có phải trang cuối? |
| `hasNext()` | Có trang sau? |
| `hasPrevious()` | Có trang trước? |

---

## 6. Ví dụ thực tế

**URL:** `/foods?page=0&size=12`

- `page=0`: trang đầu tiên.
- `size=12`: mỗi trang 12 món.
- Nếu có 50 món → `totalPages = 5` (50/12 = 4.17 → làm tròn lên 5).

**Trang 1 (page=0):** món 1-12  
**Trang 2 (page=1):** món 13-24  
**Trang 3 (page=2):** món 25-36  
...  
**Trang 5 (page=4):** món 49-50

---

## 7. Trong Thymeleaf (food-list.html)

```html
<!-- Hiển thị danh sách món của trang hiện tại -->
<div th:each="food : ${foods}">
    <!-- food là từ foodPage.getContent() -->
</div>

<!-- Phân trang: nút Previous -->
<a th:if="${currentPage > 0}"
   th:href="@{/foods(page=${currentPage - 1})}">« Trước</a>

<!-- Hiển thị số trang -->
<span>Trang <strong th:text="${currentPage + 1}">1</strong> / 
      <strong th:text="${totalPages}">5</strong></span>

<!-- Phân trang: nút Next -->
<a th:if="${currentPage < totalPages - 1}"
   th:href="@{/foods(page=${currentPage + 1})}">Sau »</a>
```

**Lưu ý:** Trong URL, `page` bắt đầu từ 0, nhưng hiển thị cho user nên dùng `currentPage + 1` (1, 2, 3...).

---

## 8. Kết hợp với lọc và tìm kiếm

Trong `FoodController.list()`:

```java
// Giữ nguyên các tham số filter khi chuyển trang
th:href="@{/foods(categoryId=${selectedCategoryId}, keyword=${keyword}, page=${currentPage + 1})}"
```

Khi user chuyển trang, các filter (categoryId, keyword) vẫn được giữ nguyên.

---

## 9. Tóm tắt luồng

1. User truy cập `/foods?page=1&categoryId=2`.
2. Controller nhận `page=1`, `categoryId=2`.
3. Tạo `Pageable = PageRequest.of(1, 12)`.
4. Gọi `FoodService.findByCategoryId(2, pageable)`.
5. Service gọi `FoodRepository.findByCategoryIdAndIsAvailableTrue(2, pageable)`.
6. Repository thực thi SQL với `LIMIT 12 OFFSET 12` (trang 1 = bỏ qua 12 dòng đầu).
7. Trả về `Page<Food>` chứa 12 món + metadata (totalPages, totalElements).
8. Controller đưa vào Model, render `food-list.html`.
9. Thymeleaf hiển thị 12 món + UI phân trang.

---

## 10. File đã tạo trong Bước 4

- **FoodController.java** – xử lý GET `/foods` với phân trang, lọc, tìm kiếm.
- **food-list.html** – form lọc, grid món, UI phân trang.
- **food.css** – CSS cho trang danh sách (form, grid, phân trang).

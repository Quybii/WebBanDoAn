# BƯỚC 2 – CẤU TRÚC PROJECT VÀ VAI TRÒ TỪNG LAYER

## 1. Cấu trúc thư mục chuẩn

```
WebBanDoAn/
├── pom.xml
├── sql/
│   └── schema.sql
├── docs/
├── src/
│   └── main/
│       ├── java/com/webbandoan/
│       │   ├── WebBanDoAnApplication.java   ← Main class
│       │   ├── controller/                   ← Layer Controller
│       │   ├── service/                      ← Layer Service
│       │   ├── repository/                  ← Layer Repository
│       │   ├── entity/                       ← Layer Entity
│       │   └── config/                       ← Cấu hình (Security, MVC, ...)
│       └── resources/
│           ├── application.properties        ← Cấu hình DB, JPA, Thymeleaf
│           ├── templates/                    ← Thymeleaf HTML (layout, trang)
│           └── static/
│               ├── css/
│               ├── js/
│               └── images/
```

---

## 2. Vai trò từng layer

### **Controller** (`controller/`)

- **Vai trò:** Nhận request từ trình duyệt (URL, form), gọi Service để xử lý, trả về tên view (Thymeleaf) hoặc redirect.
- **Không:** Không chứa logic nghiệp vụ, không gọi Repository trực tiếp.
- **Ví dụ:** `HomeController` nhận `/`, gọi `CategoryService.findAll()` và `FoodService.findFeatured()`, trả về view `"home"` với model chứa danh sách danh mục và món nổi bật.

---

### **Service** (`service/`)

- **Vai trò:** Chứa toàn bộ logic nghiệp vụ (tính toán, kiểm tra, quy tắc). Gọi Repository để đọc/ghi database.
- **Không:** Không biết HTTP (request/response), không biết view.
- **Ví dụ:** `CartService.addItem(userId, foodId, quantity)` kiểm tra món còn bán không, tìm hoặc tạo `CartItem`, cập nhật số lượng, gọi `CartItemRepository.save()`.

---

### **Repository** (`repository/`)

- **Vai trò:** Giao tiếp với database: CRUD và truy vấn. Thường là interface kế thừa `JpaRepository<Entity, Id>`.
- **Không:** Không chứa logic nghiệp vụ phức tạp, chỉ truy vấn/lưu theo yêu cầu từ Service.
- **Ví dụ:** `FoodRepository` có `findByCategoryId()`, `findByNameContaining()`, `findByIsAvailableTrue()`.

---

### **Entity** (`entity/`)

- **Vai trò:** Class Java ánh xạ 1-1 với bảng trong database. Dùng JPA annotation (@Entity, @Table, @Id, @ManyToOne, ...).
- **Không:** Không gọi Repository/Service, không xử lý HTTP.
- **Ví dụ:** `Food`, `Order`, `User` – mỗi class tương ứng một bảng.

---

### **Config** (`config/`)

- **Vai trò:** Cấu hình ứng dụng: Spring Security (đăng nhập, phân quyền), MVC (static resources, upload), Bean (PasswordEncoder, ...).
- **Ví dụ:** `SecurityConfig` cấu hình form login, URL nào cần đăng nhập, role ADMIN/USER.

---

### **templates/** (Thymeleaf)

- **Vai trò:** File HTML dùng làm view. Controller trả về tên file (ví dụ `"home"`) → Thymeleaf render `templates/home.html` với dữ liệu từ model.
- **Cấu trúc thường dùng:** `layout/header.html`, `layout/footer.html`, các trang `home.html`, `food-list.html`, `cart.html`, `admin/dashboard.html`, ...

---

### **static/** (CSS, JS, hình ảnh)

- **Vai trò:** Tài nguyên tĩnh. Trình duyệt gọi trực tiếp qua URL, ví dụ `/css/style.css`, `/js/cart.js`, `/images/logo.png`.
- **css/:** File CSS chung và từng trang (style.css, home.css, food.css, ...).
- **js/:** File JavaScript (cart.js, form validate, ...).
- **images/:** Ảnh logo, ảnh món, ảnh danh mục.

---

## 3. Luồng xử lý (Request → Response)

1. **User** gửi request (ví dụ: GET `/foods?categoryId=1&page=0`).
2. **DispatcherServlet** (Spring) gửi request tới **Controller** tương ứng (ví dụ `FoodController.list()`).
3. **Controller** gọi **Service** (ví dụ `FoodService.findByCategory(1, pageable)`).
4. **Service** gọi **Repository** (ví dụ `FoodRepository.findByCategoryId(1, pageable)`).
5. **Repository** thực thi truy vấn qua JPA, trả về **Entity** (hoặc Page&lt;Food&gt;).
6. **Service** xử lý (nếu cần) rồi trả kết quả cho Controller.
7. **Controller** đưa dữ liệu vào **Model**, trả về tên view (ví dụ `"food-list"`).
8. **Thymeleaf** render `templates/food-list.html` với model → HTML.
9. **Response** trả về trình duyệt (HTML + link tới CSS/JS/images trong `static/`).

---

## 4. Cấu hình đã tạo (Bước 2)

- **pom.xml:** Spring Boot 3.2, Web, Thymeleaf, JPA, Security, SQL Server, Validation.
- **application.properties:** Cổng 8080, URL/user/pass SQL Server, JPA (ddl-auto=none, show-sql), Thymeleaf (prefix/suffix, cache=false), encoding UTF-8.
- **WebBanDoAnApplication.java:** Class có `main()`, đánh dấu `@SpringBootApplication`.

**Lưu ý:** Đổi `spring.datasource.url`, `username`, `password` trong `application.properties` cho đúng SQL Server của bạn (tên server, tên database `webbandoan`, user/pass). Chạy `sql/schema.sql` trên database đó trước khi chạy ứng dụng.

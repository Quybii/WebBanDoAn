# BƯỚC 2 – TÓM TẮT: CẤU TRÚC PROJECT

## Đã tạo

### 1. Maven & Spring Boot

- **pom.xml**
  - Spring Boot 3.2.5, Java 17
  - spring-boot-starter-web, thymeleaf, data-jpa, security, validation
  - mssql-jdbc (SQL Server), devtools

### 2. Cấu hình & Main

- **src/main/resources/application.properties**
  - Server port 8080
  - Datasource SQL Server (url, username, password – cần đổi theo máy bạn)
  - JPA: ddl-auto=none, show-sql, SQLServerDialect
  - Thymeleaf: prefix/suffix, cache=false, UTF-8

- **src/main/java/com/webbandoan/WebBanDoAnApplication.java**
  - Class chính với `@SpringBootApplication` và `main()`

### 3. Cấu trúc package (layer)

| Package            | Vai trò                                      |
|--------------------|----------------------------------------------|
| controller/        | Nhận request, gọi Service, trả view/redirect |
| service/           | Logic nghiệp vụ, gọi Repository              |
| repository/        | Giao tiếp database (JpaRepository)          |
| entity/            | Class JPA ánh xạ bảng (đã có từ Bước 1)     |
| config/            | Cấu hình Security, MVC, Bean                |

Mỗi package có **package-info.java** mô tả ngắn vai trò.

### 4. Thư mục tài nguyên

- **templates/** – Thymeleaf HTML (sẽ thêm layout, trang trong các bước sau)
- **static/css/** – CSS
- **static/js/** – JavaScript (tạo khi cần)
- **static/images/** – Hình ảnh (tạo khi cần)

### 5. Tài liệu

- **docs/BUOC2_CAU_TRUC_PROJECT.md** – Giải thích chi tiết từng layer và luồng request → response.

---

## Chạy ứng dụng

1. Tạo database **webbandoan** trên SQL Server, chạy **sql/schema.sql**.
2. Sửa **application.properties**: `spring.datasource.url`, `username`, `password` cho đúng máy bạn.
3. Trong thư mục gốc project: `mvn spring-boot:run` (hoặc chạy class `WebBanDoAnApplication` trong IDE).

Sau khi chạy, ứng dụng chưa có trang nào (chưa có Controller/View) – sẽ làm ở Bước 3 (Trang chủ).

# 🍔 Web Bán Đồ Ăn (Food Ordering Web Application)

Dự án Website Bán Đồ Ăn được phát triển bằng **Java Spring Boot** và **Thymeleaf**, cung cấp nền tảng đặt món trực tuyến với giao diện hiện đại, tính năng giỏ hàng mượt mà, tích hợp thanh toán và quản trị đơn hàng.

## 👥 Thành viên nhóm & Phân công công việc

| STT | Thành viên | Vai trò & Nhiệm vụ chính |
|:---:|:---|:---|
| 1 | **Quý** | Backend: Xác thực (Đăng ký, Đăng nhập), Spring Security, UserService, Unit Tests Auth. |
| 2 | **Luận** | Backend: API Giỏ hàng, Checkout, OrderService, Tích hợp thanh toán MoMo, Integration Tests. |
| 3 | **Phú** | Backend: Chức năng Admin (Quản lý món, Quản lý đơn), Database Schema, Seed Data. |
| 4 | **Thân** | Frontend & QA: Giao diện (Thymeleaf, CSS, JS), Responsive (Mobile First), Custom JS (Cart, Modal), Kiểm thử thủ công. |

## 🚀 Công nghệ sử dụng

*   **Backend:** Java (JDK 17+), Spring Boot 3.x, Spring Security, Spring Data JPA.
*   **Frontend:** HTML5, CSS3, Vanilla JavaScript, Thymeleaf Template Engine.
*   **Database:** Microsoft SQL Server.
*   **Build Tool:** Maven.
*   **Tích hợp bên thứ 3:** Cổng thanh toán MoMo Sandbox, Google Maps API (Places & Geocoding).

## ✨ Tính năng nổi bật

### Dành cho Khách hàng (User)
*   Đăng ký, đăng nhập an toàn với Spring Security.
*   Xem danh sách món ăn, tìm kiếm và xem chi tiết.
*   Thêm vào giỏ hàng, cập nhật số lượng và xóa món (AJAX mượt mà, Custom Modal).
*   Thanh toán đơn hàng (Hỗ trợ COD và tích hợp MoMo).
*   Định vị địa chỉ giao hàng bằng Google Maps.
*   Xem lịch sử đơn hàng và theo dõi trạng thái.

### Dành cho Quản trị viên (Admin)
*   Dashboard quản lý tập trung.
*   Thêm, sửa, xóa món ăn (Upload hình ảnh).
*   Quản lý danh sách đơn hàng, cập nhật trạng thái đơn (Chờ xác nhận, Đang giao, Đã hoàn thành, Đã hủy).

## ⚙️ Hướng dẫn Cài đặt & Chạy dự án

### 1. Yêu cầu hệ thống (Prerequisites)
*   JDK 17 hoặc mới hơn.
*   Maven 3.6+.
*   Microsoft SQL Server (SQL Server Management Studio - SSMS).

### 2. Cài đặt Cơ sở dữ liệu (SQL Server)
1. Mở SQL Server Management Studio (SSMS) và tạo một database mới tên là: `WebBanDoAn`
2. Chạy script import dữ liệu mẫu từ file `sql/schema.sql` để tạo bảng và dữ liệu test.
3. **Cấu hình kết nối (Quan trọng):** 
   Dự án sử dụng cơ chế profile `local` để bảo mật thông tin. Bạn cần tạo một file mới tên là `application-local.properties` nằm cùng thư mục với `application.properties` (`src/main/resources/`) và điền thông tin đăng nhập SQL Server của máy bạn vào:
   ```properties
   spring.datasource.username=sa
   spring.datasource.password=mat_khau_sql_server_cua_ban
   ```

### 3. Cấu hình API Bên thứ 3
Mở file `application-local.properties` vừa tạo ở bước trên và bổ sung các API Key để test chức năng Thanh toán và Bản đồ:

*   **Cấu hình Google Maps:**
    ```properties
    google.maps.apiKey=YOUR_GOOGLE_MAPS_API_KEY
    ```
*   **Cấu hình MoMo (Môi trường Sandbox):**
    ```properties
    momo.momo-api-url=[https://test-payment.momo.vn/v2/gateway/api/create](https://test-payment.momo.vn/v2/gateway/api/create)
    momo.partner-code=YOUR_PARTNER_CODE
    momo.access-key=YOUR_ACCESS_KEY
    momo.secret-key=YOUR_SECRET_KEY
    momo.return-url=http://localhost:8080/checkout/momo-return
    momo.notify-url=http://localhost:8080/checkout/momo-notify
    ```

### 4. Khởi chạy ứng dụng
Chạy ứng dụng bằng lệnh Maven trong terminal (hoặc chạy file Application trực tiếp trong IDE):
   ```bash
   mvn spring-boot:run
   ```
Website sẽ được khởi chạy tại địa chỉ: `http://localhost:8080`

## 🧪 Tài khoản Kiểm thử (Demo Accounts)

Sử dụng các tài khoản sau để test hệ thống:

| Vai trò | Email | Mật khẩu | Chức năng kiểm thử |
| :--- | :--- | :--- | :--- |
| **Admin** | `admin@gmail.com` | `123456` | Quản lý món ăn, quản lý đơn hàng. |
| **User** | `user@gmail.com` | `123456` | Thêm giỏ hàng, đặt hàng, xem lịch sử. |

## 🎨 Ghi chú về Giao diện & QA (UI/UX)
*   **Responsive:** Dự án được thiết kế theo hướng Mobile-First. Vui lòng mở Developer Tools (F12) và chọn chế độ thiết bị di động (iPhone/Pixel) để kiểm tra giao diện dạng Card Layout và Menu tối ưu cho Mobile.
*   **JavaScript & AJAX:** Các tính năng trong Giỏ hàng (Thay đổi số lượng, Xóa món) sử dụng Fetch API kết hợp với Custom Confirm Modal, đảm bảo trải nghiệm liền mạch không cần reload trang.
*   **Kiểm thử thủ công:** Đã vượt qua 100% các Test Case cơ bản (Luồng đặt hàng, Cập nhật trạng thái Admin, Bảo mật phân quyền).

## ADD SETTING MOMO
momo.momo-api-url=https://test-payment.momo.vn/v2/gateway/api/create
momo.partner-code=MOMO
momo.access-key=F8BBA842ECF85
momo.secret-key=K951B6PE1waDMi640xX08PD3vg6EkVlz
momo.request-type=captureWallet
momo.order-type=momo_wallet
momo.auto-capture=true
momo.lang=vi
momo.pay-order-return-url=http://localhost:8080/payment/momo-return
momo.pay-order-notify-url=http://localhost:8080/payment/momo-callback

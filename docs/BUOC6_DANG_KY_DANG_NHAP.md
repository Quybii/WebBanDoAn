# BƯỚC 6 – ĐĂNG KÝ / ĐĂNG NHẬP

## Đã hoàn thành

### 1. Repository

- **RoleRepository.java** – `findByName(String name)` để lấy role USER khi đăng ký.
- **UserRepository.java** – `findByUsername(String username)`, `existsByUsername(String username)`.

### 2. Service

- **UserService.java**
  - `register(username, rawPassword, fullName, email, phone, address)`:
    - Kiểm tra username chưa tồn tại, lấy role USER từ RoleRepository.
    - Mã hóa mật khẩu bằng **BCrypt** (`PasswordEncoder`).
    - Tạo User, set role, enabled = true, lưu qua UserRepository.
  - `existsByUsername(username)` – dùng cho validate đăng ký.

### 3. Security

- **CustomUserDetailsService.java** – implements `UserDetailsService`:
  - `loadUserByUsername(username)` → load User từ UserRepository.
  - Trả về `UserDetails` (Spring Security) với authority **ROLE_USER** hoặc **ROLE_ADMIN** (từ `user.getRole().getName()`).
  - Spring Security dùng class này khi xử lý form login.

- **SecurityConfig.java** (cập nhật):
  - **PasswordEncoder** bean: `BCryptPasswordEncoder()`.
  - **Phân quyền:**
    - `permitAll`: `/`, `/foods`, `/foods/**`, `/css/**`, `/js/**`, `/images/**`, `/register`, `/login`.
    - `authenticated`: `/cart/**`, `/orders/**` (chỉ user đã đăng nhập).
    - `hasRole("ADMIN")`: `/admin/**` (chỉ ADMIN).
    - Còn lại: `authenticated`.
  - **Form login:** trang `/login`, success → `/`, failure → `/login?error=true`.
  - **Logout:** POST `/logout`, success → `/`.
  - CSRF bật (mặc định); form login và logout đều gửi CSRF token.

### 4. Controller

- **AuthController.java**
  - GET `/login` – hiển thị form đăng nhập; nếu `?error=true` thì hiển thị thông báo lỗi; nếu có `successMessage` (sau đăng ký) thì hiển thị thành công.
  - GET `/register` – hiển thị form đăng ký.
  - POST `/register` – xử lý đăng ký: validate (username, password không trống; username chưa tồn tại; mật khẩu ≥ 4 ký tự), gọi `UserService.register(...)`, redirect `/login` kèm flash message hoặc redirect `/register` kèm lỗi.

### 5. Template

- **login.html** – form POST `/login` (username, password), CSRF token, link sang đăng ký.
- **register.html** – form POST `/register` (username, password, fullName, email, phone, address), CSRF token, validate cơ bản (required, minlength), link sang đăng nhập.

### 6. Layout & CSS

- **layout/header.html** – khi đã đăng nhập: hiển thị "Xin chào, {username}" và form POST `/logout` (CSRF); khi chưa đăng nhập: hiển thị link Đăng nhập. Dùng `GlobalControllerAdvice` cung cấp `isAuthenticated`, `currentUsername`.
- **auth.css** – style cho form đăng nhập/đăng ký và nút Đăng xuất trong header.

### 7. Config bổ sung

- **GlobalControllerAdvice.java** – `@ModelAttribute("isAuthenticated")`, `@ModelAttribute("currentUsername")` để mọi view dùng chung (header).

---

## Mã hóa mật khẩu

- Dùng **BCrypt** qua `BCryptPasswordEncoder`.
- Khi đăng ký: `passwordEncoder.encode(rawPassword)` rồi lưu vào `User.password`.
- Khi đăng nhập: Spring Security so sánh mật khẩu nhập với bản đã lưu bằng BCrypt (tự động qua `UserDetailsService` + `PasswordEncoder`).

---

## Phân quyền USER / ADMIN

- Role trong DB: `roles.name` = `"USER"` hoặc `"ADMIN"`.
- Trong Spring Security: authority = `"ROLE_" + role.name` → `ROLE_USER`, `ROLE_ADMIN`.
- URL:
  - `/cart/**`, `/orders/**`: cần đăng nhập (bất kỳ role).
  - `/admin/**`: chỉ `hasRole("ADMIN")` (tức ROLE_ADMIN).

---

## Luồng đăng nhập

1. User mở GET `/login` → hiển thị form.
2. User gửi POST `/login` (username, password, CSRF) → Spring Security nhận.
3. Security gọi `CustomUserDetailsService.loadUserByUsername(username)` → lấy `UserDetails` (password đã mã hóa trong DB).
4. Security so sánh mật khẩu nhập với password trong DB (BCrypt).
5. Thành công → tạo session, redirect `/`. Thất bại → redirect `/login?error=true`.

---

## Luồng đăng ký

1. User mở GET `/register` → hiển thị form.
2. User gửi POST `/register` (username, password, fullName, email, phone, address).
3. AuthController validate → gọi `UserService.register(...)`.
4. UserService: kiểm tra username chưa tồn tại, lấy role USER, encode password, lưu User.
5. Redirect `/login` với flash "Đăng ký thành công. Vui lòng đăng nhập."

---

## Lưu ý

- Dữ liệu mẫu trong `sql/schema.sql` có user `admin` / `user1` với password cần được mã hóa BCrypt khi chạy app (hoặc đăng ký tài khoản mới rồi đăng nhập).
- Tài khoản mới đăng ký luôn có role **USER**. Để có ADMIN cần cập nhật trong DB hoặc tạo tài khoản admin qua script/seed.

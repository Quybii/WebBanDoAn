# 📋 Hướng dẫn Setup Local Development

## ⚠️ Quan trọng: Database Credentials

File `application.properties` không chứa credentials vì lí do bảo mật.

## 🚀 Hướng dẫn Setup cho mỗi thành viên team

### Step 1: Clone project
```bash
git clone <repository-url>
cd WebBanDoAn
```

### Step 2: Tạo file cấu hình cục bộ
Tạo file mới: `src/main/resources/application-local.properties`

**Hoặc copy từ template:**
```bash
# Windows PowerShell
cp src/main/resources/application-local.properties src/main/resources/application-local.properties
```

### Step 3: Cấu hình SQL Server của bạn
Mở file `src/main/resources/application-local.properties` và sửa:

```properties
# Điền thông tin SQL Server của máy bạn
spring.datasource.url=jdbc:sqlserver://YOUR_SERVER:1433;databaseName=WebBanDoAn;encrypt=true;trustServerCertificate=true
spring.datasource.username=YOUR_USERNAME
spring.datasource.password=YOUR_PASSWORD
```

**Ví dụ:**
```properties
spring.datasource.url=jdbc:sqlserver://LAPTOP-TRAN:1433;databaseName=WebBanDoAn;encrypt=true;trustServerCertificate=true
spring.datasource.username=sa
spring.datasource.password=MyPassword123
```

### Step 4: Run ứng dụng
```bash
# Maven
mvn spring-boot:run

# Hoặc từ IDE: Right-click → Run
```

---

## ✅ Lợi ích của cách này

- ✅ **Bảo mật**: Credentials không commit lên Git
- ✅ **Độc lập**: Mỗi dev dùng server của mình mà không ảnh hưởng nhau
- ✅ **Dễ quản lý**: File `.gitignore` tự động bỏ qua config cục bộ
- ✅ **Dễ onboard**: Thành viên mới chỉ cần copy template và fill thông tin

---

## 📝 Danh sách file quan trọng

| File | Mục đích | Commit? |
|------|---------|---------|
| `application.properties` | Cấu hình chung | ✅ Yes |
| `application-local.properties` | Cấu hình cục bộ (credentials) | ❌ No |
| `.gitignore` | Quy tắc ignore | ✅ Yes |

---

## 🔧 Troubleshooting

**Q: Chạy ứng dụng nhưng không kết nối được database?**
- A: Kiểm tra file `application-local.properties` có tồn tại không
- A: Kiểm tra thông tin URL, username, password có đúng không

**Q: Mải quên tạo file `application-local.properties`?**
- A: Tạo file theo hướng dẫn ở trên, sau đó restart ứng dụng

**Q: Có thể dùng environment variables thay vì file không?**
- A: Có! Xem phần "Cách thay thế" ở đầu hướng dẫn quản lý credentials

---

## 📬 Ghi chú

- **Mỗi lần cập nhật `application.properties` từ Git**, chỉ cần pull - file local của bạn không bị ảnh hưởng
- **Nếu cần share cấu hình**, dùng `application-local.properties` template và gửi riêng cho thành viên


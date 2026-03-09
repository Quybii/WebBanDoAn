package com.webbandoan.service;

import com.webbandoan.entity.Role;
import com.webbandoan.entity.User;
import com.webbandoan.repository.RoleRepository;
import com.webbandoan.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service: User.
 * Logic nghiệp vụ: đăng ký (mã hóa mật khẩu BCrypt, gán role USER).
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Đăng ký tài khoản mới.
     * Mật khẩu được mã hóa BCrypt, role mặc định là USER.
     *
     * @return User đã lưu, hoặc null nếu username đã tồn tại / role USER không tìm thấy
     */
    @Transactional
    public User register(String username, String rawPassword, String fullName, String email, String phone, String address) {
        if (userRepository.existsByUsername(username)) {
            return null;
        }
        Role userRole = roleRepository.findByName("USER").orElse(null);
        if (userRole == null) {
            return null;
        }
        User user = new User();
        user.setUsername(username.trim());
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setFullName(fullName != null ? fullName.trim() : null);
        user.setEmail(email != null ? email.trim() : null);
        user.setPhone(phone != null ? phone.trim() : null);
        user.setAddress(address != null ? address.trim() : null);
        user.setRole(userRole);
        user.setEnabled(true);
        return userRepository.save(user);
    }

    /**
     * Kiểm tra username đã tồn tại chưa.
     */
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return username != null && userRepository.existsByUsername(username.trim());
    }
}

package com.chonhanh.service;

import com.chonhanh.dto.*;
import com.chonhanh.entity.*;
import com.chonhanh.repository.UserRepository;
import com.chonhanh.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    // --- ĐĂNG KÝ ---
    @Transactional
    public UserResponseDTO register(UserRegisterDTO dto) {
        if (userRepository.existsByPhone(dto.getPhone())) {
            throw new RuntimeException("Số điện thoại đã được đăng ký!");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng!");
        }

        User user = new User();
        user.setFullName(dto.getFullName());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setRole(Role.user); // Mặc định là user
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());
        
        return mapToResponseDTO(userRepository.save(user));
    }


 // Thêm đoạn này vào UserService.java
    public User authenticate(String username, String password) {
        // 1. Tìm user theo số điện thoại, nếu không thấy thì tìm theo email
        User user = userRepository.findByPhone(username)
                .orElse(userRepository.findByEmail(username).orElse(null));

        // 2. Kiểm tra user tồn tại và mật khẩu khớp (dùng passwordEncoder để so sánh mã hóa)
        if (user != null && passwordEncoder.matches(password, user.getPasswordHash())) {
            return user; // Đăng nhập thành công
        }
        return null; // Đăng nhập thất bại
    }
    // --- DÀNH CHO ADMIN ---
    
    public Page<UserResponseDTO> getAllUsersForAdmin(String keyword, Pageable pageable) {
        Page<User> users;
        if (keyword != null && !keyword.trim().isEmpty()) {
            users = userRepository.searchUsers(keyword, pageable);
        } else {
            users = userRepository.findAll(pageable);
        }
        return users.map(this::mapToResponseDTO);
    }

    @Transactional
    public UserResponseDTO toggleUserLock(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));
        
        if (user.getRole() == Role.admin) {
            throw new RuntimeException("Không thể khóa tài khoản Quản trị viên!");
        }

        user.setIsActive(!user.getIsActive());
        return mapToResponseDTO(userRepository.save(user));
    }

    @Transactional
    public UserResponseDTO updateUser(Long userId, UserRequestDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));

        // Cập nhật thông tin cơ bản
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setIsActive(dto.getIsActive());

        // Xử lý cập nhật Role an toàn (Chuyển từ String DTO sang Enum Role)
        if (dto.getRole() != null) {
            try {
                // Chuyển chuỗi thành Enum tương ứng (phù hợp với cách khai báo Role của bạn)
                user.setRole(Role.valueOf(dto.getRole().toLowerCase())); // hoặc .toUpperCase() tùy enum của bạn
            } catch (IllegalArgumentException e) {
                // Fallback nếu chuỗi không khớp, tránh văng lỗi 500
                user.setRole(Role.user); 
            }
        }

        return mapToResponseDTO(userRepository.save(user));
    }

    // --- HÀM HỖ TRỢ DUY NHẤT ---
    private UserResponseDTO mapToResponseDTO(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setFullName(user.getFullName());
        dto.setPhone(user.getPhone());
        dto.setEmail(user.getEmail());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setRole(user.getRole());
        dto.setIsActive(user.getIsActive());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
    private final String UPLOAD_DIR = "uploads/avatars/";

    public void updateProfile(String email, UserProfileUpdateDto dto) {
        // Tìm user theo email đăng nhập hiện tại
    	User user = userRepository.findByEmail(email)
    	        .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));
        // Cập nhật thông tin cơ bản
        user.setFullName(dto.getFullName());
        user.setPhone(dto.getPhone());

        // Xử lý upload ảnh đại diện mới (nếu có chọn file)
        MultipartFile avatarFile = dto.getAvatar();
        if (avatarFile != null && !avatarFile.isEmpty()) {
            try {
                // Tạo thư mục nếu chưa có
                Path uploadPath = Paths.get(UPLOAD_DIR);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                // Đổi tên file để tránh trùng lặp
                String fileName = UUID.randomUUID().toString() + "_" + avatarFile.getOriginalFilename();
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(avatarFile.getInputStream(), filePath);

                // Lưu đường dẫn vào database
                user.setAvatarUrl("/avatars/" + fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Lưu xuống cơ sở dữ liệu PostgreSQL
        userRepository.save(user);
    }
    

    // Hàm xử lý đổi mật khẩu
    public void changePassword(String email, PasswordChangeDto dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));

        // 1. Kiểm tra mật khẩu hiện tại có đúng không
        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Mật khẩu hiện tại không chính xác!");
        }

        // 2. Kiểm tra mật khẩu mới và xác nhận mật khẩu có trùng nhau không
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new RuntimeException("Mật khẩu mới và xác nhận mật khẩu không khớp!");
        }

        // 3. Mã hóa và lưu mật khẩu mới
        user.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }
}
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

        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setIsActive(dto.getIsActive());

//        // Xử lý cập nhật Role an toàn
//        try {
//            user.setRole(Role.valueOf(dto.getRole().toUpperCase()));
//        } catch (IllegalArgumentException | NullPointerException e) {
//            throw new RuntimeException("Role không hợp lệ!");
//        }

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
}
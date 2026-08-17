package com.chonhanh.repository;

import com.chonhanh.entity.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Tìm user bằng số điện thoại (Dùng khi Đăng nhập)
    Optional<User> findByPhone(String phone);
    
    // Tìm user bằng email
    Optional<User> findByEmail(String email);
    
    // Kiểm tra số điện thoại / email đã tồn tại chưa (Dùng khi Đăng ký)
    boolean existsByPhone(String phone);
    boolean existsByEmail(String email);
 // Tìm kiếm user theo tên hoặc sđt (có phân trang)
    @Query("SELECT u FROM User u WHERE u.fullName LIKE %:keyword% OR u.phone LIKE %:keyword%")
    Page<User> searchUsers(@Param("keyword") String keyword, Pageable pageable);
 // Thêm vào UserRepository
    Page<User> findByFullNameContainingIgnoreCase(String fullName, Pageable pageable);
}
package com.chonhanh.controller;

import com.chonhanh.dto.CategoryDTO;
import com.chonhanh.dto.CategoryRequestDTO;
import com.chonhanh.dto.DashboardDTO;
import com.chonhanh.dto.PostCreateDTO;
import com.chonhanh.dto.PostResponseDTO;
import com.chonhanh.dto.UserRequestDTO;
import com.chonhanh.dto.UserResponseDTO;
import com.chonhanh.entity.PostStatus;
import com.chonhanh.entity.User;
import com.chonhanh.repository.UserRepository;
import com.chonhanh.service.AdminService;
import com.chonhanh.service.PostService;
import com.chonhanh.service.UserService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final com.chonhanh.service.CategoryService categoryService;
    private final UserRepository userRepository;
    private final PostService postService;
    private final AdminService adminService;
    private final UserService userService; 
 // API: GET http://localhost:8080/api/admin/dashboard
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardDTO> getDashboardStats() {
        DashboardDTO stats = adminService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }
    // API: Lấy danh sách tin đang chờ duyệt
    @GetMapping("/posts/pending")
    public ResponseEntity<Page<PostResponseDTO>> getPendingPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());
        return ResponseEntity.ok(postService.getPendingPosts(pageable));
    }

    // API: Duyệt tin (Chuyển status thành ACTIVE)
    @PutMapping("/posts/{postId}/approve")
    public ResponseEntity<PostResponseDTO> approvePost(@PathVariable Long postId) {
        PostResponseDTO updatedPost = postService.updatePostStatusByAdmin(postId, PostStatus.active);
        return ResponseEntity.ok(updatedPost);
    }

    // API: Từ chối tin (Chuyển status thành REJECTED)
    @PutMapping("/posts/{postId}/reject")
    public ResponseEntity<PostResponseDTO> rejectPost(@PathVariable Long postId) {
        PostResponseDTO updatedPost = postService.updatePostStatusByAdmin(postId, PostStatus.rejected);
        return ResponseEntity.ok(updatedPost);
    }
  
    // ==========================================
    // API QUẢN LÝ THÀNH VIÊN
    // ==========================================

    // API: Lấy danh sách thành viên
    // GET http://localhost:8080/api/admin/users?keyword=nam&page=0&size=10
  
    
 // Nhớ thêm dòng này ở đầu class AdminController

    // ==========================================
    // API QUẢN LÝ DANH MỤC
    // ==========================================

    // API: Thêm danh mục mới
    // POST http://localhost:8080/api/admin/categories
    @PostMapping("/categories")
    public ResponseEntity<com.chonhanh.dto.CategoryDTO> createCategory(@RequestBody com.chonhanh.dto.CategoryRequestDTO dto) {
        return ResponseEntity.ok(categoryService.createCategory(dto));
    }

    // API: Cập nhật danh mục
    // PUT http://localhost:8080/api/admin/categories/5
    @PutMapping("/categories/{id}")
    public ResponseEntity<CategoryDTO> updateCategory(
            @PathVariable Long id, 
            @RequestBody CategoryRequestDTO dto) {
        
        // THÊM DÒNG LOG NÀY
        System.out.println("DEBUG NHẬN DỮ LIỆU: Tên=" + dto.getName() + ", ParentID=" + dto.getParentId());
        
        return ResponseEntity.ok(categoryService.updateCategory(id, dto));
    }

    // API: Xóa danh mục
    // DELETE http://localhost:8080/api/admin/categories/5
    @DeleteMapping("/categories/{id}")
    public ResponseEntity<String> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok("Xóa danh mục thành công!");
    }



    // Xóa bài viết
    @DeleteMapping("/posts/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.ok().build();
    }

    // Phê duyệt hoặc từ chối (Dùng cho Admin)
    @PutMapping("/posts/{id}/status")
    public ResponseEntity<PostResponseDTO> updateStatus(
            @PathVariable Long id, 
            @RequestParam PostStatus status) {
        return ResponseEntity.ok(postService.updatePostStatusByAdmin(id, status));
    }
 // Sửa đoạn này trong AdminController.java
    @PutMapping("/users/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable Long id, @RequestBody UserRequestDTO dto) { // <-- Phải là UserRequestDTO ở đây
        // Gọi service cập nhật với đúng kiểu dto mà service mong đợi
        return ResponseEntity.ok(userService.updateUser(id, dto));
    }



}
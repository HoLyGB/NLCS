package com.chonhanh.controller;

import com.chonhanh.dto.AuthResponseDTO;
import com.chonhanh.dto.PostCreateDTO;
import com.chonhanh.dto.PostResponseDTO;
import com.chonhanh.dto.UserLoginDTO;
import com.chonhanh.dto.UserRegisterDTO;
import com.chonhanh.dto.UserResponseDTO;
import com.chonhanh.entity.Post;
import com.chonhanh.entity.User;
import com.chonhanh.repository.CategoryRepository;
import com.chonhanh.service.CategoryService;
import com.chonhanh.service.PostService;
import com.chonhanh.service.UserService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
@Controller // PHẢI LÀ @Controller (để render file HTML)
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
//
//    // API Đăng ký: POST http://localhost:8080/api/users/register
//    @PostMapping("/register")
//    public ResponseEntity<UserResponseDTO> register(@RequestBody UserRegisterDTO dto) {
//        UserResponseDTO response = userService.register(dto);
//        return new ResponseEntity<>(response, HttpStatus.CREATED);
//    }

    // API Đăng nhập: POST http://localhost:8080/api/users/login
//    @PostMapping("/login")
//    public ResponseEntity<AuthResponseDTO> login(@RequestBody UserLoginDTO dto) {
//        // Đổi từ UserResponseDTO sang AuthResponseDTO
//        AuthResponseDTO response = userService.login(dto);
//        return ResponseEntity.ok(response);
//    }
    private final PostService postService;

    @GetMapping("/my-posts")
    public String myPosts(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size, // Hiển thị 5 tin mỗi trang
            HttpSession session,
            Model model) {
        
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/dangnhap";

        Page<Post> postPage = postService.getMyPostsPaged(user.getId(), keyword, page, size);
        int totalPages = postPage.getTotalPages();
        if (totalPages > 0) {
            int startPage = Math.max(0, page - 2);
            int endPage = Math.min(totalPages - 1, page + 2);
            model.addAttribute("startPage", startPage);
            model.addAttribute("endPage", endPage);
        }
        model.addAttribute("myPosts", postPage);
        model.addAttribute("keyword", keyword);
        
        return "my-posts"; // Tên file HTML của bạn
    }
    
    @GetMapping("/post/delete/{id}")
    public String deletePost(@PathVariable("id") Long postId, HttpSession session, RedirectAttributes ra) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/dangnhap";

        try {
            postService.deletePost(postId);
            ra.addFlashAttribute("successMessage", "Xóa tin thành công!");
        } catch (Exception e) {
        	e.printStackTrace();
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        
        return "redirect:/user/my-posts";
    }
    

    private final CategoryRepository categoryRepository;
    private final CategoryService categoryService;
    // 1. Hiển thị trang chỉnh sửa
    @GetMapping("/post/edit/{id}")
    public String showEditForm(@PathVariable("id") Long postId, HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";

        PostResponseDTO post = postService.getPostDetailById(postId); // Hoặc hàm lấy post theo id
        model.addAttribute("categories", categoryService.getAllCategories());
        // Kiểm tra quyền sở hữu trước khi cho sửa giao diện
        if (!post.getSellerId().equals(user.getId())) {
            return "redirect:/user/my-posts?error=unauthorized";
        }

        model.addAttribute("postDTO", post);
        model.addAttribute("categories", categoryRepository.findAll());
        return "post-edit"; // Tên file html giao diện sửa
    }

    // 2. Xử lý lưu thông tin sau khi sửa
    @PostMapping("/post/edit/{id}")
    public String updatePost(@PathVariable("id") Long postId, 
                             @ModelAttribute("postDTO") PostCreateDTO dto, 
                             HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";

        postService.updatePost(postId, user.getId(), dto);
        return "redirect:/user/my-posts?success=updated";
    }
    
}
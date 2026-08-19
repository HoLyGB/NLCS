package com.chonhanh.controller;

import com.chonhanh.dto.CategoryDTO;
import com.chonhanh.dto.UserResponseDTO;
import com.chonhanh.entity.Post;
import com.chonhanh.entity.PostStatus;
import com.chonhanh.entity.Role;
import com.chonhanh.entity.User;
import com.chonhanh.repository.CategoryRepository;
import com.chonhanh.repository.PostRepository;
import com.chonhanh.repository.UserRepository;
import com.chonhanh.service.CategoryService;
import com.chonhanh.service.PostService;
import com.chonhanh.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminPageController {

    private final PostService postService;
    private final UserService userService;
    private final CategoryService categoryService;
    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    // ==========================================
    // HÀM HỖ TRỢ KIỂM TRA QUYỀN ADMIN CHUNG
    // ==========================================
    private String checkAdminAuth(HttpSession session) {
        User currentUser = (User) session.getAttribute("loggedInUser");
        
        // 1. Nếu chưa đăng nhập -> Chuyển hướng về trang đăng nhập
        if (currentUser == null) {
            return "redirect:/dangnhap";
        }
        
        // 2. Nếu đã đăng nhập nhưng không phải là ADMIN -> Chuyển hướng về trang chủ
        if (currentUser.getRole() != Role.admin) { 
            return "redirect:/"; 
        }
        
        // 3. Hợp lệ (Là Admin) -> Trả về null để cho phép chạy tiếp
        return null;
    }

    // ==========================================
    // 1. TRANG DASHBOARD
    // ==========================================
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        // Kiểm tra bảo mật Admin
        String authRedirect = checkAdminAuth(session);
        if (authRedirect != null) return authRedirect;

        // Thống kê số liệu cho 4 ô
        long activePostsCount = postRepository.countByPostStatus(PostStatus.active);
        long pendingPostsCount = postRepository.countByPostStatus(PostStatus.pending);
        long totalCategoriesCount = categoryRepository.count();
        long totalUsersCount = userRepository.count();
        
        // Lấy danh sách tin đăng mới nhất (5 tin gần nhất)
        List<Post> recentPosts = postRepository.findTop5ByOrderByIdDesc();

        model.addAttribute("activePostsCount", activePostsCount);
        model.addAttribute("pendingPostsCount", pendingPostsCount);
        model.addAttribute("totalCategoriesCount", totalCategoriesCount);
        model.addAttribute("totalUsersCount", totalUsersCount);
        model.addAttribute("recentPosts", recentPosts);

        return "admin/dashboard";
    }

    // ==========================================
    // 2. TRANG QUẢN LÝ NGƯỜI DÙNG
    // ==========================================
    @GetMapping("/users")
    public String listUsers(HttpSession session, 
                            Model model, 
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(required = false) String search) {
        // Kiểm tra bảo mật Admin
        String authRedirect = checkAdminAuth(session);
        if (authRedirect != null) return authRedirect;

        int size = 10;
        Pageable pageable = PageRequest.of(page, size);
        
        Page<User> userPage;
        if (search != null && !search.isEmpty()) {
            userPage = userRepository.findByFullNameContainingIgnoreCase(search, pageable);
        } else {
            userPage = userRepository.findAll(pageable);
        }

        model.addAttribute("users", userPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", userPage.getTotalPages());
        model.addAttribute("search", search);
        
        return "admin/users";
    }

    // ==========================================
    // 3. TRANG QUẢN LÝ DANH MỤC
    // ==========================================
    @GetMapping("/categories")
    public String listCategories(HttpSession session, Model model, @RequestParam(required = false) String search) {
        // Kiểm tra bảo mật Admin
        String authRedirect = checkAdminAuth(session);
        if (authRedirect != null) return authRedirect;

        List<CategoryDTO> categories;
        if (search != null && !search.isEmpty()) {
            categories = categoryService.searchCategories(search);
        } else {
            categories = categoryService.getAllCategories();
        }
        
        model.addAttribute("categories", categories);
        model.addAttribute("search", search);
        model.addAttribute("categoryDTO", new CategoryDTO());
        return "admin/categories";
    }

    // ==========================================
    // 4. TRANG QUẢN LÝ BÀI VIẾT
    // ==========================================
    @GetMapping("/posts")
    public String listPosts(HttpSession session, Model model) {
        // Kiểm tra bảo mật Admin
        String authRedirect = checkAdminAuth(session);
        if (authRedirect != null) return authRedirect;

        model.addAttribute("pendingPosts", postService.getPostsByStatus(PostStatus.pending));
        model.addAttribute("processedPosts", postService.getProcessedPosts());
        return "admin/posts";
    }
}
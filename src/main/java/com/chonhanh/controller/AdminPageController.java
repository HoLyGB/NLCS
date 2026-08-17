package com.chonhanh.controller;

import com.chonhanh.dto.CategoryDTO;
import com.chonhanh.dto.UserResponseDTO;
import com.chonhanh.entity.Post;
import com.chonhanh.entity.PostStatus;
import com.chonhanh.entity.User;
import com.chonhanh.repository.CategoryRepository;
import com.chonhanh.repository.PostRepository;
import com.chonhanh.repository.UserRepository;
import com.chonhanh.service.CategoryService;
import com.chonhanh.service.PostService;
import com.chonhanh.service.UserService;
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
	private final PostService postService; // Thêm service này
    private final UserService userService;
    private final CategoryService categoryService;
    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    // Đường dẫn để truy cập: http://localhost:8080/admin/dashboard
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // 1. Thống kê số liệu cho 4 ô
    	
    	long activePostsCount = postRepository.countByPostStatus(PostStatus.active);
        long pendingPostsCount = postRepository.countByPostStatus(PostStatus.pending);
        long totalCategoriesCount = categoryRepository.count(); // Tổng danh mục
        long totalUsersCount = userRepository.count(); // Tổng thành viên
        // 2. Lấy danh sách tin đăng mới nhất (ví dụ lấy 5 tin gần nhất)
        List<Post> recentPosts = postRepository.findTop5ByOrderByIdDesc();

        // 3. Đẩy dữ liệu ra view
        model.addAttribute("activePostsCount", activePostsCount);
        model.addAttribute("pendingPostsCount", pendingPostsCount);
        model.addAttribute("totalCategoriesCount", totalCategoriesCount);
        model.addAttribute("totalUsersCount", totalUsersCount);
        model.addAttribute("recentPosts", recentPosts);

        return "admin/dashboard"; // Trỏ đến file html của bạn
    }
 // Trong file AdminPageController.java
//    @GetMapping("/users")
//    public String showUsers(Model model) {
//        // Lấy danh sách users (ví dụ lấy 100 người đầu tiên)
//        Page<UserResponseDTO> usersPage = userService.getAllUsersForAdmin(null, PageRequest.of(0, 100));
//        model.addAttribute("users", usersPage.getContent());
//        return "admin/users"; 
//    }
    private final UserRepository userRepository;

    @GetMapping("/users")
    public String listUsers(Model model, 
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(required = false) String search) {
        
        int size = 10; // Giờ bạn chỉnh size ở đây sẽ ăn ngay
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
        
        return "admin/users"; // Trả về file HTML
    }


 // Trong AdminCategoryWebController.java
    @GetMapping("/categories")
    public String listCategories(Model model, @RequestParam(required = false) String search) {
        List<CategoryDTO> categories;
        
        if (search != null && !search.isEmpty()) {
            categories = categoryService.searchCategories(search);
        } else {
            categories = categoryService.getAllCategories();
        }
        
        model.addAttribute("categories", categories);
        model.addAttribute("search", search); // Truyền search xuống view để hiển thị lại trên ô input
        return "admin/categories";
    }
    @GetMapping("/posts")
    public String listPosts(Model model) {
        // Giả sử bạn có hàm lấy danh sách bài viết theo status trong PostService
        // Bạn cần tạo thêm các hàm tương ứng trong PostService để lấy danh sách này
        model.addAttribute("pendingPosts", postService.getPostsByStatus(PostStatus.pending));
        model.addAttribute("processedPosts", postService.getProcessedPosts()); // List bài Active + Rejected
        return "admin/posts";
    }
}
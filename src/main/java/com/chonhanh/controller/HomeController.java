package com.chonhanh.controller;

import com.chonhanh.dto.PostCreateDTO;
import com.chonhanh.dto.PostResponseDTO;
import com.chonhanh.entity.Post;
import com.chonhanh.entity.User;
import com.chonhanh.repository.CategoryRepository;
import com.chonhanh.repository.PostRepository;
import com.chonhanh.service.CategoryService;
import com.chonhanh.service.FileUploadService;
import com.chonhanh.service.PostService;
import com.chonhanh.service.WishlistService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tomcat.util.http.fileupload.FileUpload;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final PostService postService;
    private final CategoryService categoryService;
    private final FileUploadService fileUploadService;
    private final PostRepository postRepository;
//    @GetMapping({"/", "/trangchu"})
//    public String index(Model model, 
//            @PageableDefault(size = 9) Pageable pageable) {
//        // 1. Lấy toàn bộ danh mục để hiển thị ở Sidebar (theo logic bạn đã có)
//    	model.addAttribute("categories", categoryService.getRootCategories());
//
//        // 2. Lấy danh sách tin đăng đã duyệt (status = active)
//        // Sử dụng PageRequest.of(0, 12) để lấy 12 tin đầu tiên của trang đầu tiên
//        // Sắp xếp theo ngày tạo giảm dần (tin mới nhất lên đầu)
//        model.addAttribute("posts", postService.getHomepagePosts(pageable).getContent());
//
//        // Trả về file index.html trong thư mục templates
//        return "index";
//    }
    @GetMapping("/post/create")
    public String showCreateForm(Model model, HttpSession session) {
        // Kiểm tra đăng nhập
        if (session.getAttribute("loggedInUser") == null) {
            return "redirect:/dangnhap";
        }
        model.addAttribute("postDTO", new PostCreateDTO());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "post-create"; // File post-create.html
    }

    @PostMapping("/post/create")
    public String processCreate(@ModelAttribute("postDTO") PostCreateDTO dto, 
                                @RequestParam("files") List<MultipartFile> files, // Thêm tham số này
                                HttpSession session, 
                                RedirectAttributes ra) {
        
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/dangnhap";

        try {
            // 1. Lưu các file ảnh vào folder, lấy về list URL
            List<String> imageUrls = fileUploadService.saveFiles(files); 
            dto.setImageUrls(imageUrls); // Gán list URL vào DTO

            // 2. Gọi service để lưu bài đăng
            postService.createPost(user.getId(), dto);
            
            ra.addFlashAttribute("successMsg", "Đăng tin kèm ảnh thành công!");
            return "redirect:/";
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Lỗi: " + e.getMessage());
            return "redirect:/post/create";
        }
    }
    @GetMapping("/category/{slug}")
    public String getPostsByCategory(@PathVariable("slug") String slug, 
                                     Model model, 
                                     @PageableDefault(size = 9) Pageable pageable) {
        
        // 1. Sửa thành Page<PostResponseDTO> cho khớp với Service trả về
        Page<PostResponseDTO> postPage = postService.getPostsByCategorySlug(slug, pageable);
        
        // 2. Xử lý phân trang thông minh
        int totalPages = postPage.getTotalPages();
        if (totalPages > 0) {
            int startPage = Math.max(0, pageable.getPageNumber() - 2);
            int endPage = Math.min(totalPages - 1, pageable.getPageNumber() + 2);
            
            model.addAttribute("startPage", startPage);
            model.addAttribute("endPage", endPage);
        }

        // 3. Đẩy dữ liệu ra view
        model.addAttribute("posts", postPage);
        model.addAttribute("categories", categoryService.getRootCategories());
        model.addAttribute("currentCategorySlug", slug);
        
        return "index";
    }
    
    @RestController // Dùng RestController để trả về JSON
    @RequestMapping("/api/wishlist")
    @RequiredArgsConstructor
    public class WishlistController {
        private final WishlistService wishlistService;

        @PostMapping("/toggle/{postId}")
        public ResponseEntity<Map<String, Object>> toggle(@PathVariable Long postId, HttpSession session) {
            User user = (User) session.getAttribute("loggedInUser");
            if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

            boolean isSaved = wishlistService.toggleWishlist(user, postId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("saved", isSaved);
            return ResponseEntity.ok(response);
        }
    }
    @GetMapping("/post/{id}")
    public String postDetail(@PathVariable Long id, Model model) {
        // Gọi service lấy dữ liệu (hàm này mình đã làm ở bước trước)
        PostResponseDTO post = postService.getPostDetailById(id);
        model.addAttribute("post", post);
        return "post-detail";
    }
    

    @GetMapping("/search")
    public String searchPosts(@RequestParam(required = false) String keyword, Model model) {
        // Lấy danh sách bài viết theo từ khóa
        List<Post> posts = postService.searchByKeyword(keyword);
        
        // Đẩy dữ liệu ra view
    	model.addAttribute("categories", categoryService.getRootCategories());
        model.addAttribute("posts", posts);
        model.addAttribute("keyword", keyword); // Truyền lại từ khóa để hiển thị trên ô input
        
        return "index"; // Bạn có thể dùng chung trang "index" hoặc tạo trang "search-results" riêng
    }
    
    @GetMapping({"/", "/trangchu", "/search/advanced"})
    public String homeOrSearch(
            @RequestParam(required = false) String priceRange, // 👈 Đón nhận tham số priceRange từ thẻ select của HTML
            @RequestParam(required = false) String province,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            Model model) {
        
        Double minPrice = null;
        Double maxPrice = null;
        
        // Xử lý tách chuỗi từ dropdown priceRange (Ví dụ: "0-500000" thành min = 0, max = 500000)
        if (priceRange != null && !priceRange.trim().isEmpty() && priceRange.contains("-")) {
            try {
                String[] parts = priceRange.split("-");
                if (parts.length >= 1 && !parts[0].isEmpty()) {
                    minPrice = Double.parseDouble(parts[0]);
                }
                if (parts.length >= 2 && !parts[1].isEmpty()) {
                    maxPrice = Double.parseDouble(parts[1]);
                }
            } catch (Exception e) {
                // Đề phòng lỗi định dạng số
            }
        }
        
        if (province != null && province.trim().isEmpty()) province = null;
        
        // 1. Gọi Service tìm kiếm nâng cao
        Page<Post> postPage = postService.searchAdvanced(minPrice, maxPrice, province, categoryId, page, size);
        
        // 2. Xử lý phân trang rút gọn thông minh
        int totalPages = postPage.getTotalPages();
        if (totalPages > 0) {
            int startPage = Math.max(0, page - 2);
            int endPage = Math.min(totalPages - 1, page + 2);
            
            model.addAttribute("startPage", startPage);
            model.addAttribute("endPage", endPage);
        }

        // 3. Đẩy dữ liệu ra giao diện View
        model.addAttribute("categories", categoryService.getRootCategories());
        model.addAttribute("posts", postPage);
        model.addAttribute("province", province);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("priceRange", priceRange); // Giữ lại giá trị select trên form
        
        return "index";
    }
}
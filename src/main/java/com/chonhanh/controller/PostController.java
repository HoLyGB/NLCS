package com.chonhanh.controller;

import com.chonhanh.dto.PostCreateDTO;
import com.chonhanh.dto.PostResponseDTO;
import com.chonhanh.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // API Đăng tin mới: POST http://localhost:8080/api/posts?userId=1
    // (Lưu ý: Tạm thời truyền userId qua query param để test, thực tế sẽ lấy từ Token)
    @PostMapping
    public ResponseEntity<PostResponseDTO> createPost(
            @RequestParam Long userId,
            @RequestBody PostCreateDTO dto) {
        PostResponseDTO response = postService.createPost(userId, dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // API Lấy danh sách tin trang chủ (Có phân trang): GET http://localhost:8080/api/posts?page=0&size=20
    @GetMapping
    public ResponseEntity<Page<PostResponseDTO>> getHomepagePosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        // Sắp xếp tin mới nhất lên đầu
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<PostResponseDTO> posts = postService.getHomepagePosts(pageable);
        
        return ResponseEntity.ok(posts);
    }

    // API Lấy chi tiết 1 tin đăng: GET http://localhost:8080/api/posts/ban-xe-wave-alpha
    @GetMapping("/{slug}")
    public ResponseEntity<PostResponseDTO> getPostDetail(@PathVariable String slug) {
        PostResponseDTO post = postService.getPostDetail(slug);
        return ResponseEntity.ok(post);
    }
}
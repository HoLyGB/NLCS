package com.chonhanh.service;

import com.chonhanh.dto.*;
import com.chonhanh.entity.*;
import com.chonhanh.repository.*;
import lombok.RequiredArgsConstructor;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.text.Normalizer;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PostImageRepository postImageRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final ObjectMapper objectMapper = new ObjectMapper(); // Khai báo ObjectMapper
    // --- CÁC HÀM CHO NGƯỜI DÙNG ---

    @Transactional
    public PostResponseDTO createPost(Long userId, PostCreateDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại"));

        Post post = new Post();
        post.setUser(user);
        post.setCategory(category);
        post.setTitle(dto.getTitle());
        post.setDescription(dto.getDescription());
        post.setPrice(dto.getPrice());
        post.setProvince(dto.getProvince());
        post.setDistrict(dto.getDistrict());
        if (dto.getAttributes() != null && !dto.getAttributes().isEmpty()) {
            try {
                Map<String, Object> attrMap = objectMapper.readValue(dto.getAttributes(), 
                                                new TypeReference<Map<String,Object>>(){});
                post.setAttributes(attrMap); 
            } catch (Exception e) {
                throw new RuntimeException("Dữ liệu attributes không đúng định dạng JSON: " + e.getMessage());
            }
        }
        post.setStatus(PostStatus.pending);
     // Trong hàm createPost của PostService
    
        String slug = generateSlug(dto.getTitle()) + "-" + System.currentTimeMillis();
        post.setSlug(slug);
        
        Post savedPost = postRepository.save(post);

    
        if (dto.getImageUrls() != null && !dto.getImageUrls().isEmpty()) {
            for (int i = 0; i < dto.getImageUrls().size(); i++) {
                PostImage img = new PostImage();
                img.setPost(savedPost); // Gán bài đăng vào ảnh
                img.setImageUrl(dto.getImageUrls().get(i)); // URL ảnh
                
                // Logic cực hay: Ảnh đầu tiên (index 0) sẽ là ảnh bìa
                img.setIsPrimary(i == 0); 
                
                postImageRepository.save(img);
            }
        }
        return mapToResponseDTO(savedPost);
    }

    public Page<PostResponseDTO> getHomepagePosts(Pageable pageable) {
        return postRepository.findByStatus(PostStatus.active, pageable).map(this::mapToResponseDTO);
    }

    public PostResponseDTO getPostDetail(String slug) {
        Post post = postRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tin đăng"));
        return mapToResponseDTO(post);
    }
 // Thêm hàm này vào PostService.java
    public PostResponseDTO getPostDetailById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tin đăng"));
        return mapToResponseDTO(post);
    }
    // --- CÁC HÀM CHO ADMIN (DUYỆT & XÓA) ---

    public Page<PostResponseDTO> getPendingPosts(Pageable pageable) {
        return postRepository.findByStatus(PostStatus.pending, pageable).map(this::mapToResponseDTO);
    }

    @Transactional
    public PostResponseDTO updatePostStatusByAdmin(Long postId, PostStatus newStatus) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tin đăng"));
        
        if (newStatus == PostStatus.active || newStatus == PostStatus.rejected) {
            post.setStatus(newStatus);
            return mapToResponseDTO(postRepository.save(post));
        } else {
            throw new RuntimeException("Trạng thái cập nhật không hợp lệ!");
        }
    }
    

    @Transactional
    public void deletePost(Long postId) {
        messageRepository.deleteMessagesByPostId(postId);

        conversationRepository.deleteByPostId(postId);

        // 3. Xóa ảnh của bài viết
        postImageRepository.deleteByPostId(postId);

        // 4. Tìm và xóa bài viết
        Optional<Post> postOpt = postRepository.findById(postId);
        postOpt.ifPresent(post -> {
            postRepository.delete(post);
        });
    }

    // --- HÀM HỖ TRỢ ---

    private PostResponseDTO mapToResponseDTO(Post post) {
        PostResponseDTO dto = new PostResponseDTO();
        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setSlug(post.getSlug());
        dto.setDescription(post.getDescription());
        dto.setPrice(post.getPrice());
        dto.setStatus(post.getStatus());
        dto.setProvince(post.getProvince());
        dto.setDistrict(post.getDistrict());
        dto.setAttributes(post.getAttributes());
        dto.setCreatedAt(post.getCreatedAt());
        dto.setCategoryId(post.getCategory().getId());
        User seller = post.getUser();
        dto.setSellerId(seller.getId());
        dto.setSellerName(seller.getFullName());
        dto.setSellerPhone(seller.getPhone());
        dto.setSellerAvatar(seller.getAvatarUrl());
   

     // Trong PostService (hàm getPostDetailById hoặc mapToResponseDTO)
     ObjectMapper objectMapper = new ObjectMapper();
     try {
         if (post.getAttributes() != null) {
             // Chuyển Map thành chuỗi JSON để hiện lên textarea
             String jsonStr = objectMapper.writeValueAsString(post.getAttributes());
             dto.setAttributesJson(jsonStr);
         }
     } catch (Exception e) {
         dto.setAttributesJson("");
     }
        Category cat = post.getCategory();
        dto.setCategoryName(cat.getName());
        dto.setCategorySlug(cat.getSlug());
    
        List<PostImage> images = postImageRepository.findByPostId(post.getId());
        dto.setImages(images.stream().map(img -> {
            PostImageDTO imgDto = new PostImageDTO();
            imgDto.setId(img.getId());
            imgDto.setImageUrl(img.getImageUrl());
            imgDto.setIsPrimary(img.getIsPrimary());
            return imgDto;
        }).collect(Collectors.toList()));

        return dto;
    }

    private String generateSlug(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String noTone = pattern.matcher(normalized).replaceAll("").toLowerCase();
        return noTone.replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
    }
 // Thêm vào PostService.java

 // Hàm lấy bài theo trạng thái cụ thể (Pending, Active, Rejected)
 public List<PostResponseDTO> getPostsByStatus(PostStatus status) {
     return postRepository.findByStatus(status) // Đảm bảo PostRepository có hàm findByStatus trả về List<Post>
             .stream()
             .map(this::mapToResponseDTO)
             .collect(Collectors.toList());
 }

 // Hàm lấy các bài đã xử lý (Active hoặc Rejected)
 public List<PostResponseDTO> getProcessedPosts() {
     return postRepository.findByStatusIn(List.of(PostStatus.active, PostStatus.rejected))
             .stream()
             .map(this::mapToResponseDTO)
             .collect(Collectors.toList());
 }
 
 public Page<PostResponseDTO> getPostsByCategorySlug(String categorySlug, Pageable pageable) {
	    return postRepository.findByCategorySlugAndStatus(categorySlug, PostStatus.active, pageable)
	            .map(this::mapToResponseDTO);
	}
//Trong PostService.java
public List<PostResponseDTO> getPostsByUserId(Long userId) {
  return postRepository.findByUserIdOrderByCreatedAtDesc(userId)
          .stream()
          .map(this::mapToResponseDTO)
          .collect(Collectors.toList());
}

@Transactional
public void updatePost(Long postId, Long userId, PostCreateDTO dto) {
    Post post = postRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy tin đăng"));

    // Kiểm tra quyền sở hữu
    if (!post.getUser().getId().equals(userId)) {
        throw new RuntimeException("Bạn không có quyền chỉnh sửa bài viết này!");
    }
    ObjectMapper objectMapper = new ObjectMapper();
    try {
        if (dto.getAttributesJson() != null && !dto.getAttributesJson().isEmpty()) {
            // Chuyển chuỗi JSON từ textarea thành Map<String, Object> để lưu vào DB
            Map<String, Object> attributesMap = objectMapper.readValue(
                dto.getAttributesJson(), 
                new TypeReference<Map<String, Object>>(){}
            );
            post.setAttributes(attributesMap);
        } else {
            post.setAttributes(null); // Hoặc giữ nguyên tùy ý bạn
        }
    } catch (Exception e) {
        throw new RuntimeException("Định dạng JSON không hợp lệ, vui lòng kiểm tra lại!");
    }
    // Cập nhật các trường thông tin cơ bản
    post.setTitle(dto.getTitle());
    post.setDescription(dto.getDescription());
    post.setPrice(dto.getPrice());
    post.setProvince(dto.getProvince());
    post.setDistrict(dto.getDistrict());
    
    // Nếu đổi danh mục
    if (!post.getCategory().getId().equals(dto.getCategoryId())) {
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại"));
        post.setCategory(category);
    }

    // Lưu lại thông tin bài viết
    postRepository.save(post);
    
    // (Tùy chọn) Nếu bạn muốn cập nhật lại ảnh: 
    // Bạn có thể xóa ảnh cũ trong postImageRepository rồi thêm danh sách ảnh mới từ dto.getImageUrls() vào.
  
}
@Transactional 
public Page<Post> searchAdvanced(Double minPrice, Double maxPrice, String province, Long categoryId, int page, int size) {
    if (province != null && province.trim().isEmpty()) province = null;
    
    Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
    
    return postRepository.searchAdvanced(minPrice, maxPrice, province, categoryId, pageable);
}

public List<Post> searchByKeyword(String keyword) {
    if (keyword == null || keyword.trim().isEmpty()) {
        return postRepository.findAll(); // Nếu không nhập gì mà bấm tìm kiếm thì trả về tất cả
    }
    return postRepository.searchByKeyword(keyword.trim());
}

public Page<Post> getMyPostsPaged(Long userId, String keyword, int page, int size) {
    if (keyword != null && keyword.trim().isEmpty()) {
        keyword = null;
    }
    Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
    return postRepository.findMyPostsWithSearch(userId, keyword, pageable);
}

public List<Post> getSavedPostsByUserId(Long userId) {
    return postRepository.findSavedPostsByUserId(userId);
}
}
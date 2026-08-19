package com.chonhanh.repository;

import com.chonhanh.entity.Post;
import com.chonhanh.entity.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    
    // Tìm tin đăng theo Slug chuẩn SEO
    Optional<Post> findBySlug(String slug);
    
    // Lấy danh sách tin đang hiển thị (status = 'active') để đưa ra Trang chủ
    Page<Post> findByStatus(PostStatus status, Pageable pageable);
    
    // Lấy danh sách tin của một User cụ thể (Xem trong trang Quản lý tin đã đăng)
    Page<Post> findByUserId(Long userId, Pageable pageable);
    
    Page<Post> findByCategorySlugAndStatus(String categorySlug, PostStatus status, Pageable pageable);
    // Lọc tin đăng theo Tỉnh/Thành phố và trạng thái
    Page<Post> findByProvinceAndStatus(String province, PostStatus status, Pageable pageable);
    
 // Thêm vào file PostRepository.java
 // 👇 Truy vấn tường minh theo String
    @Query("SELECT count(p) FROM Post p WHERE p.status = :status")
    long countByPostStatus(@Param("status") PostStatus status);

    // 👇 Thay đổi phương thức lấy tin mới nhất cũng dùng Query cho chắc chắn
    @Query("SELECT p FROM Post p ORDER BY p.id DESC")
    List<Post> findRecentPosts(Pageable pageable);
 // Kiểm tra xem có tin đăng nào đang dùng danh mục này không
    boolean existsByCategoryId(Long categoryId);
    List<Post> findByStatus(PostStatus status);
    List<Post> findByStatusIn(List<PostStatus> statuses);
    List<Post> findByUserIdOrderByCreatedAtDesc(Long userId);
    @Query("SELECT p FROM Post p WHERE p.status = com.chonhanh.entity.PostStatus.active " +
    	       "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
    	       "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
    	       "AND (:province IS NULL OR p.province LIKE %:province%) " +
    	       "AND (:categoryId IS NULL OR p.category.id = :categoryId)")
    	Page<Post> searchAdvanced(@Param("minPrice") Double minPrice, 
    	                          @Param("maxPrice") Double maxPrice, 
    	                          @Param("province") String province, 
    	                          @Param("categoryId") Long categoryId, 
    	                          Pageable pageable);
    
    @Query("SELECT p FROM Post p WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Post> searchByKeyword(@Param("keyword") String keyword);
    
    @Query("SELECT p FROM Post p WHERE p.user.id = :userId AND (:keyword IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Post> findMyPostsWithSearch(@Param("userId") Long userId, @Param("keyword") String keyword, Pageable pageable);
    
    List<Post> findTop5ByOrderByIdDesc();
    
 // Sửa lại câu lệnh query chuẩn theo thuộc tính của SavedPost Entity (sp.post và sp.user.id)
    @Query("SELECT sp.post FROM SavedPost sp WHERE sp.user.id = :userId")
    List<Post> findSavedPostsByUserId(@Param("userId") Long userId);
    
}
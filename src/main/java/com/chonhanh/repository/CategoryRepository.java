package com.chonhanh.repository;

import com.chonhanh.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    // Lấy tất cả danh mục gốc (Danh mục cha cao nhất)
    List<Category> findByParentIsNull();
    
    // Tìm danh mục theo Slug (Để làm URL thân thiện chuẩn SEO)
    Optional<Category> findBySlug(String slug);
    
 // Dùng JPQL để JOIN bảng Category và Post, đếm số post trong mỗi category
    @Query("SELECT c.name, COUNT(p.id) FROM Category c LEFT JOIN Post p ON c.id = p.category.id GROUP BY c.id")
    List<Object[]> countPostsByCategory();
 // Kiểm tra xem danh mục này có đang chứa danh mục con nào không
    boolean existsByParentId(Long parentId);
 // Trong CategoryRepository.java
    List<Category> findByNameContainingIgnoreCase(String name);
}
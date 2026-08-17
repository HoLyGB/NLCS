package com.chonhanh.repository;

import com.chonhanh.entity.Post;
import com.chonhanh.entity.SavedPost;
import com.chonhanh.entity.SavedPostId;
import com.chonhanh.entity.User;

import jakarta.transaction.Transactional;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SavedPostRepository extends JpaRepository<SavedPost, Long> {
    // Kiểm tra xem user đã lưu tin này chưa
    boolean existsByUserAndPost(User user, Post post);
    
    // Xóa tin đã lưu
    @Transactional
    void deleteByUserAndPost(User user, Post post);
 // Thêm vào WishlistRepository.java
    boolean existsByUserIdAndPostId(Long userId, Long postId);
    // Lấy danh sách tin đã lưu của user
    List<SavedPost> findByUser(User user);
}
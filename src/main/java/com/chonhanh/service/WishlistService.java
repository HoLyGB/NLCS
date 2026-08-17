package com.chonhanh.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.chonhanh.entity.Post;
import com.chonhanh.entity.SavedPost;
import com.chonhanh.entity.User;
import com.chonhanh.repository.PostRepository;
import com.chonhanh.repository.SavedPostRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WishlistService {
    private final SavedPostRepository savedPostRepository;
    private final PostRepository postRepository;
 // Thêm vào WishlistService.java
    public boolean isPostSaved(Long postId, Long userId) {
        if (userId == null) return false;
        return savedPostRepository.existsByUserIdAndPostId(userId, postId);
    }
    public boolean toggleWishlist(User user, Long postId) {
        Post post = postRepository.findById(postId).orElseThrow();
        
        if (savedPostRepository.existsByUserAndPost(user, post)) {
            savedPostRepository.deleteByUserAndPost(user, post);
            return false; // Đã hủy lưu
        } else {
            SavedPost savedPost = new SavedPost();
            savedPost.setUser(user);
            savedPost.setPost(post);
            savedPost.setCreatedAt(LocalDateTime.now());
            savedPostRepository.save(savedPost);
            return true; // Đã lưu
        }
    }
}

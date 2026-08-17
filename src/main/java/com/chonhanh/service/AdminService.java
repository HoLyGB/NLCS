package com.chonhanh.service;

import com.chonhanh.dto.DashboardDTO;
import com.chonhanh.entity.PostStatus;
import com.chonhanh.repository.PostRepository;
import com.chonhanh.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;

    public DashboardDTO getDashboardStats() {
        DashboardDTO dto = new DashboardDTO();
        
        // Đếm số lượng User
        dto.setTotalUsers(userRepository.count());
        
        // Đếm số lượng Post tổng quát
        dto.setTotalPosts(postRepository.count());
        
        // Đếm Post theo từng trạng thái cụ thể
//        dto.setPendingPosts(postRepository.countByStatus(PostStatus.pending));
//        dto.setActivePosts(postRepository.countByStatus(PostStatus.active));
//        dto.setRejectedPosts(postRepository.countByStatus(PostStatus.rejected));
//        dto.setSoldPosts(postRepository.countByStatus(PostStatus.sold));
//        
        return dto;
    }
}
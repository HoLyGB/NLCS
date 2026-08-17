package com.chonhanh.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ConversationResponseDTO {
    private Long id;
    
    // Thông tin của bài đăng mà 2 người đang chat
    private Long postId;
    private String postTitle;
    private String postImageUrl; // Lấy ảnh bìa của bài đăng
    
    // Thông tin của người đối diện (Partner) đang chat với mình
    private Long partnerId;
    private String partnerName;
    private String partnerAvatar;
    
    // Lấy trước dòng tin nhắn cuối cùng để hiển thị ở ngoài hộp thoại
    private String lastMessage; 
    private LocalDateTime lastUpdatedAt;
}
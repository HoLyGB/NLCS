package com.chonhanh.dto;

import lombok.Data;

@Data
public class MessageSendDTO {
    // Nếu là tin nhắn đầu tiên, frontend sẽ truyền postId và receiverId
    // Nếu đã chat rồi, frontend chỉ cần truyền conversationId
    private Long conversationId; 
    private Long postId;
    private Long receiverId; 
    private String content;
}
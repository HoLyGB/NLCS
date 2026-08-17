package com.chonhanh.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MessageResponseDTO {
    private Long id;
    private Long senderId;
    private String content;
    private LocalDateTime createdAt;
}
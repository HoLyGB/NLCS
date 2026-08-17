package com.chonhanh.dto;
import lombok.Data;

@Data
public class AuthResponseDTO {
    private String token;          // Thẻ từ JWT
    private UserResponseDTO user;  // Thông tin user
}
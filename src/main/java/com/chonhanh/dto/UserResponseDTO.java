package com.chonhanh.dto;

import com.chonhanh.entity.Role;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserResponseDTO {
    private Long id;
    private String fullName;
    private String phone;
    private String email;
    private String avatarUrl;
    private Role role;
    private LocalDateTime createdAt;
    private Boolean isActive;
}
package com.chonhanh.dto;

import lombok.Data;

@Data
public class UserRequestDTO {
    private String fullName;
    private String email;
    private String phone;
    private Boolean isActive; // dùng boolean cho is_active (1 hoặc 0)
    private String role;
}

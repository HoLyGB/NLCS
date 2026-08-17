package com.chonhanh.dto;

import lombok.Data;

@Data
public class UserRegisterDTO {
    private String fullName;
    private String phone;
    private String email;
    private String password;
}
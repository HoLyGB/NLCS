package com.chonhanh.dto;

import org.springframework.web.multipart.MultipartFile;

public class UserProfileUpdateDto {
    private String full_Name;
    private String phone;
    private MultipartFile avatar; // File ảnh đại diện tải lên

    // Getters and Setters
    public String getFullName() {
        return full_Name;
    }

    public void setFullName(String fullName) {
        this.full_Name = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public MultipartFile getAvatar() {
        return avatar;
    }

    public void setAvatar(MultipartFile avatar) {
        this.avatar = avatar;
    }
}
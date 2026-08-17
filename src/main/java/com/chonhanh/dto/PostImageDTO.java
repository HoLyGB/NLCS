package com.chonhanh.dto;

import lombok.Data;

@Data
public class PostImageDTO {
    private Long id;
    private String imageUrl;
    private Boolean isPrimary;
}
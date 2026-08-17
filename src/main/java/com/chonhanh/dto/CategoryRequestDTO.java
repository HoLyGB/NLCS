package com.chonhanh.dto;

import lombok.Data;

@Data
public class CategoryRequestDTO {
    private String name;
    private Long parentId; // Có thể để trống (null) nếu đây là danh mục gốc (ví dụ: "Xe cộ")
}
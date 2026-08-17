package com.chonhanh.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class PostCreateDTO {
    private Long categoryId;
    private String title;
    private String description;
    private BigDecimal price;
    private String province;
    private String district;
    private String attributes; // Đổi từ Map<String, Object> thành String    
    // Khi đăng tin, frontend thường upload ảnh lên server/cloud trước, 
    // sau đó gửi kèm mảng các URL ảnh về cho Backend lưu.
    private List<String> imageUrls; 
    private LocalDateTime createdAt;
 // Trong PostCreateDTO.java
    private String attributesJson; // Đảm bảo khai báo biến này

    // Thêm Getter này vào
    public String getAttributesJson() {
        return attributesJson;
    }

    // Thêm Setter này vào
    public void setAttributesJson(String attributesJson) {
        this.attributesJson = attributesJson;
    }
}
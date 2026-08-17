package com.chonhanh.dto;

import com.chonhanh.entity.Category;
import com.chonhanh.entity.PostStatus;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Getter
@Setter
public class PostResponseDTO {
    private Long id;
    private String title;
    private String slug;
    private String description;
    private BigDecimal price;
    private PostStatus status;
    private String province;
    private String district;
    private Map<String, Object> attributes;
    private LocalDateTime createdAt;
    
    // Gộp luôn thông tin người bán vào để hiển thị (thay vì trả nguyên object User)
    private Long sellerId;
    private String sellerName;
    private String sellerPhone;
    private String sellerAvatar;
    
    // Thông tin danh mục
    private String categoryName;
    private String categorySlug;

    // Danh sách hình ảnh của tin đăng này
    private List<PostImageDTO> images;
    private Long categoryId;

 // Getter và Setter
 public Long getCategoryId() {
     return categoryId;
 }
 public void setCategoryId(Long categoryId) {
     this.categoryId = categoryId;
 }
 
//Trong PostResponseDTO.java (hoặc PostCreateDTO)
private String attributesJson; // Dùng riêng để hiển thị lên <textarea>

public String getAttributesJson() { return attributesJson; }
public void setAttributesJson(String attributesJson) { this.attributesJson = attributesJson; }
}
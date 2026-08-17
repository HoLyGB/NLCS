package com.chonhanh.dto;

import lombok.Data;
import java.util.List;

@Data
public class CategoryDTO {
    private Long id;
    private String name;
    private Long parentId;
    private String slug;
    //Chứa danh sách các danh mục con bên trong nó
    private List<CategoryDTO> subCategories; 
}
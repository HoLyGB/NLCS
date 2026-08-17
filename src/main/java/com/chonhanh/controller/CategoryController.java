package com.chonhanh.controller;

import com.chonhanh.dto.CategoryDTO;
import com.chonhanh.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    // API Lấy cây danh mục: GET http://localhost:8080/api/categories/tree
    @GetMapping("/tree")
    public ResponseEntity<List<CategoryDTO>> getCategoryTree() {
        List<CategoryDTO> categories = categoryService.getCategoryTree();
        return ResponseEntity.ok(categories);
    }
}
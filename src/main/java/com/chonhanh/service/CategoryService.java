package com.chonhanh.service;

import com.chonhanh.dto.CategoryDTO;
import com.chonhanh.dto.CategoryRequestDTO;
import com.chonhanh.entity.Category;
import com.chonhanh.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final com.chonhanh.repository.PostRepository postRepository;
    public List<CategoryDTO> getCategoryTree() {
        // Chỉ lấy các danh mục cha (parent_id = null)
        List<Category> rootCategories = categoryRepository.findByParentIsNull();
        return rootCategories.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // Hàm ánh xạ đệ quy để tự động lấy luôn cả danh mục con
 // Hàm ánh xạ đệ quy để tự động lấy luôn cả danh mục con
    private CategoryDTO mapToDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setSlug(category.getSlug());

        // BỔ SUNG ĐOẠN NÀY ĐỂ GÁN PARENT ID
        if (category.getParent() != null) {
            dto.setParentId(category.getParent().getId());
        }

        if (category.getSubCategories() != null && !category.getSubCategories().isEmpty()) {
            List<CategoryDTO> subDTOs = category.getSubCategories().stream()
                    .map(this::mapToDTO) // Gọi lại chính nó
                    .collect(Collectors.toList());
            dto.setSubCategories(subDTOs);
        }
        return dto;
    }
    


    // 1. Thêm danh mục mới
    public CategoryDTO createCategory(CategoryRequestDTO dto) {
        Category category = new Category();
        category.setName(dto.getName());
        
        // TỰ ĐỘNG SINH SLUG TỪ NAME
        category.setSlug(toSlug(dto.getName()));
        
        if (dto.getParentId() != null) {
            Category parent = categoryRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new RuntimeException("Danh mục cha không tồn tại!"));
            category.setParent(parent);
        }
        
        return mapToDTO(categoryRepository.save(category));
    }

    // 2. Cập nhật tên / Đổi danh mục cha
    public CategoryDTO updateCategory(Long id, CategoryRequestDTO dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại!"));
        
        category.setName(dto.getName());
        
        if (dto.getParentId() != null) {
            Category parent = categoryRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new RuntimeException("Danh mục cha không tồn tại!"));
            category.setParent(parent);
        } else {
            category.setParent(null); // Biến nó thành danh mục gốc
        }
        
        return mapToDTO(categoryRepository.save(category));
    }

    // 3. Xóa danh mục (Kiểm tra điều kiện an toàn)
    public void deleteCategory(Long id) {
        // Kiểm tra xem có danh mục con không
        if (categoryRepository.existsByParentId(id)) {
            throw new RuntimeException("Không thể xóa! Danh mục này đang chứa danh mục con.");
        }
        // Kiểm tra xem có bài đăng nào thuộc danh mục này không
        if (postRepository.existsByCategoryId(id)) {
            throw new RuntimeException("Không thể xóa! Đang có tin đăng thuộc danh mục này.");
        }
        
        // An toàn thì tiến hành xóa
        categoryRepository.deleteById(id);
    }
 // Thêm hàm này vào CategoryService
    public List<CategoryDTO> getAllCategories() {
    	
        return categoryRepository.findAll().stream()
                .map(this::mapToDTO) // Dùng lại hàm mapToDTO bạn đã có sẵn
                .collect(Collectors.toList());
    }
 // Hàm hỗ trợ tạo slug đơn giản
    private String toSlug(String input) {
        String slug = input.toLowerCase().replaceAll("\\s+", "-"); // Thay khoảng trắng bằng dấu gạch ngang
        // Bạn có thể thêm xử lý bỏ dấu tiếng Việt ở đây nếu muốn
        return slug;
    }
 // Trong CategoryService.java
    public List<CategoryDTO> searchCategories(String keyword) {
        return categoryRepository.findByNameContainingIgnoreCase(keyword).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    public List<Category> getRootCategories() {
        // Giả sử bạn có hàm findByParentIsNull trong Repository
        return categoryRepository.findByParentIsNull();
    }
}
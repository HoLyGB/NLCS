package com.chonhanh.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileUploadService {

    private final String UPLOAD_DIR = "src/main/resources/static/uploads/";

    public List<String> saveFiles(List<MultipartFile> files) throws IOException {
        List<String> imageUrls = new ArrayList<>();
        
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                // Tạo tên file ngẫu nhiên để tránh trùng
                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                Path path = Paths.get(UPLOAD_DIR + fileName);
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                
                // Trả về đường dẫn để lưu vào DB (ví dụ: /uploads/12345_tenanh.jpg)
                imageUrls.add("/uploads/" + fileName);
            }
        }
        return imageUrls;
    }
}
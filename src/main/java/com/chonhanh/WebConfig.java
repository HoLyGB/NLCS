package com.chonhanh;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Lấy đường dẫn tuyệt đối của thư mục uploads trên máy
        Path uploadDir = Paths.get("uploads/avatars");
        String uploadPath = uploadDir.toFile().getAbsolutePath();

        // Ánh xạ URL /avatars/** vào thư mục thực tế trên ổ cứng
        registry.addResourceHandler("/avatars/**")
                .addResourceLocations("file:" + uploadPath + "/");
    }
}

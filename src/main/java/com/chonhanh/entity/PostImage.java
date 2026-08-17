package com.chonhanh.entity;

import java.util.List;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "post_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "is_primary")
    private Boolean isPrimary;

    @ManyToOne // Mối quan hệ N-1: Nhiều ảnh thuộc về 1 bài đăng
    @JoinColumn(name = "post_id") // Khóa ngoại trỏ về bảng Post
    private Post post; // Chỉ cần 1 thuộc tính post này thôi
 // Trong class Post.java

}
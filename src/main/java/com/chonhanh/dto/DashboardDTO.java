package com.chonhanh.dto;

import lombok.Data;

@Data
public class DashboardDTO {
    private long totalUsers;         // Tổng số thành viên
    private long totalPosts;         // Tổng số tin đăng trên hệ thống
    private long pendingPosts;       // Số tin đang chờ duyệt
    private long activePosts;        // Số tin đang hiển thị
    private long rejectedPosts;      // Số tin bị từ chối
    private long soldPosts;          // Số tin đã bán thành công
}
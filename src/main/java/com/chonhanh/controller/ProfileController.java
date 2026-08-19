package com.chonhanh.controller;

import jakarta.servlet.http.HttpSession;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.chonhanh.dto.PasswordChangeDto;
import com.chonhanh.dto.UserProfileUpdateDto;
import com.chonhanh.entity.Post;
import com.chonhanh.entity.User;
import com.chonhanh.repository.UserRepository;
import com.chonhanh.service.PostService;
import com.chonhanh.service.UserService;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final PostService postService;

    // Dùng Constructor Injection (Spring Boot tự động hiểu và tiêm bean vào đây)
    public ProfileController(UserService userService, UserRepository userRepository, PostService postService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.postService= postService;
    }

    @GetMapping
    public String viewProfile(HttpSession session, Model model) {
        User sessionUser = (User) session.getAttribute("loggedInUser");
        if (sessionUser == null) {
            return "redirect:/dangnhap";
        }

        // Lúc này userRepository chắc chắn không bị null nữa
        User user = userRepository.findByEmail(sessionUser.getEmail()).orElse(null);
        if (user == null) {
            return "redirect:/dangnhap";
        }

        model.addAttribute("user", user);
        return "profile-edit";
    }

    @PostMapping("/update")
    public String updateProfile(@ModelAttribute UserProfileUpdateDto dto,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        User sessionUser = (User) session.getAttribute("loggedInUser");
        if (sessionUser == null) {
            return "redirect:/dangnhap";
        }

        try {
            userService.updateProfile(sessionUser.getEmail(), dto);
            
            // Cập nhật lại session bằng userRepository đã tiêm
            User updatedUser = userRepository.findByEmail(sessionUser.getEmail()).orElse(null);
            session.setAttribute("loggedInUser", updatedUser);

            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cập nhật thất bại: " + e.getMessage());
        }
        return "redirect:/profile";
    }

    @PostMapping("/change-password")
    public String changePassword(@ModelAttribute PasswordChangeDto dto,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        User sessionUser = (User) session.getAttribute("loggedInUser");
        if (sessionUser == null) {
            return "redirect:/dangnhap";
        }

        try {
            userService.changePassword(sessionUser.getEmail(), dto);
            redirectAttributes.addFlashAttribute("successPasswordMessage", "Đổi mật khẩu thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorPasswordMessage", "Đổi mật khẩu thất bại: " + e.getMessage());
        }
        return "redirect:/profile";
  }
    @GetMapping("/saved-posts")
    public String viewSavedPosts(HttpSession session, Model model) {
        // Kiểm tra đăng nhập
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/dangnhap";
        }

        // Lấy danh sách tin đã lưu
        List<Post> savedPosts = postService.getSavedPostsByUserId(user.getId());
        model.addAttribute("savedPosts", savedPosts);

        return "saved-posts"; // Trả về file HTML hiển thị danh sách tin đã lưu
    }
}
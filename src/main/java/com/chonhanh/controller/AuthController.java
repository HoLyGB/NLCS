package com.chonhanh.controller;

import com.chonhanh.service.UserService;
import com.chonhanh.dto.UserRegisterDTO;
import com.chonhanh.entity.User; // Dòng này giúp Java nhận diện được lớp User
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/dangnhap")
    public String showLoginPage() {
        return "login";
    }

    @GetMapping("/dangky")
    public String showRegisterPage() {
        return "register";
    }
    @PostMapping("/dangnhap")
    public String handleLogin(@RequestParam String username,
                              @RequestParam String password,
                              HttpSession session,
                              Model model) {
        
        // Kiểm tra thông tin trong Service
        User user = userService.authenticate(username, password);

        if (user != null) {
            // Lưu thông tin người dùng vào Session
        	System.out.println("ROLE THỰC TẾ TRONG DATABASE LÀ: " + user.getRole());
            session.setAttribute("loggedInUser", user);

            // KIỂM TRA QUYỀN (ROLE) ĐỂ ĐIỀU HƯỚNG PHÙ HỢP
            // Giả sử cột role lưu chuỗi là "ADMIN" hoặc "ROLE_ADMIN"
            if ("ADMIN".equalsIgnoreCase(String.valueOf(user.getRole()))) {
                return "redirect:/admin/dashboard"; // Chuyển hướng sang trang quản trị của Admin
            } else {
                return "redirect:/trangchu"; // Chuyển hướng sang trang chủ của User thường
            }

        } else {
            // Nếu sai mật khẩu hoặc user, báo lỗi
            model.addAttribute("error", "Số điện thoại/Email hoặc mật khẩu không đúng!");
            return "login"; // Trả về trang đăng nhập
        }
    }
    // Đón dữ liệu từ Form Đăng ký
    @PostMapping("/dangky") // Đảm bảo bạn dùng @PostMapping cho form đăng ký
    public String processRegister(
            @RequestParam("fullName") String fullName,
            @RequestParam("phone") String phone,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("confirmPassword") String confirmPassword, 
            RedirectAttributes redirectAttributes) {
        
        try {
            // 1. Tạo DTO và set dữ liệu từ form
        	if (!password.equals(confirmPassword)) {
                throw new RuntimeException("Mật khẩu xác nhận không khớp!");
            }
            UserRegisterDTO dto = new UserRegisterDTO();
            dto.setFullName(fullName);
            dto.setPhone(phone);
            dto.setEmail(email);
            dto.setPassword(password);
            
            // 2. Gọi Service với DTO
            userService.register(dto);
            
            // 3. Nếu thành công, chuyển hướng
            redirectAttributes.addFlashAttribute("successMsg", "Đăng ký thành công! Vui lòng đăng nhập.");
            return "redirect:/dangnhap";
            
        } catch (Exception e) {
            // 4. Nếu thất bại, báo lỗi
            redirectAttributes.addFlashAttribute("errorMsg", "Đăng ký thất bại: " + e.getMessage());
            return "redirect:/dangky";
        }
    }
 // Thêm vào AuthController.java hoặc LoginController.java
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        // Hủy bỏ toàn bộ session
        session.invalidate();
        // Chuyển hướng về trang chủ
        return "redirect:/";
    }
}
package com.chonhanh.security;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthFilter jwtAuthFilter;

    // Cấu hình thuật toán băm mật khẩu
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        // Truyền thẳng userDetailsService vào dấu () thay vì dùng hàm set bên dưới
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        
        // Bây giờ chỉ cần set thêm PasswordEncoder là đủ
        authProvider.setPasswordEncoder(passwordEncoder());
        
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    // NỘI QUY HỆ THỐNG - ĐÃ CẬP NHẬT CHUẨN SPRING BOOT 3 (LAMBDA DSL)
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Tắt CSRF và CORS bằng cú pháp mới
            .csrf(AbstractHttpConfigurer::disable)
            .cors(AbstractHttpConfigurer::disable)
            .logout(logout -> logout
                    .logoutUrl("/logout") // Đường dẫn kích hoạt đăng xuất
                    .logoutSuccessUrl("/dangnhap?logout") // Hoặc đổi thành "/" nếu muốn về trang chủ
                    .permitAll()
                )
            // Cấu hình Session không lưu trạng thái (Stateless) vì đã dùng JWT
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Phân quyền các API
            
            .authorizeHttpRequests(auth -> auth
//            		.requestMatchers("/admin/**", "/dangnhap", "/dangky","/register-process").permitAll()
//                .requestMatchers("/api/users/login", "/api/users/register", "/api/categories/**").permitAll()
//                .requestMatchers(HttpMethod.GET, "/api/posts/**").permitAll()
//                .requestMatchers("/api/admin/**").hasRole("ADMIN")
//                .anyRequest().permitAll()
            		.requestMatchers("/**").permitAll()
            		
            		
            );

        http.authenticationProvider(authenticationProvider());
        
        // Thêm màng lọc JWT đứng trước để kiểm tra Token
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
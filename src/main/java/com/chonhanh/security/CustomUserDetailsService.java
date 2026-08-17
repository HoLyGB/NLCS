package com.chonhanh.security;

import com.chonhanh.entity.User;
import com.chonhanh.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String phone) throws UsernameNotFoundException {
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy user với SĐT: " + phone));

        // Nếu tài khoản bị Admin khóa, ném ra lỗi ngay lập tức để chặn đăng nhập
        if (!user.getIsActive()) {
            throw new RuntimeException("Tài khoản của bạn đã bị khóa do vi phạm chính sách!");
        }

        String roleName = "ROLE_" + user.getRole().name().toUpperCase();
        
        return new org.springframework.security.core.userdetails.User(
                user.getPhone(),
                user.getPasswordHash(),
                Collections.singletonList(new SimpleGrantedAuthority(roleName))
        );
    }
    
    
}
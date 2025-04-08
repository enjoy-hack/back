package com.example.smartair.service.LoginService;

import com.example.smartair.entity.CustomUserDetails;
import com.example.smartair.entity.User;
import com.example.smartair.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override // 사용자 정보를 불러와서 UserDetails로 반환
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("사용자 정보 확인" + username);
        //DB에서 조회
        Optional<User> userData = userRepository.findByUsername(username);
        System.out.println("DB 조회 결과: " + userData);
        if (userData.isEmpty()) {
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다");
        }
        System.out.println("userData");
        User user = userData.get();
        //UserDetails에 담아서 return하면 AutneticationManager가 검증 함

        System.out.println("담아서 반환");
        return new CustomUserDetails(user);
    }
}

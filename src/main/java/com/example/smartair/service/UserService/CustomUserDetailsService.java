package com.example.smartair.service.UserService;

import com.example.smartair.entity.login.CustomUserDetails;
import com.example.smartair.entity.user.User;
import com.example.smartair.repository.userRepository.UserRepository;
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

    @Override // 사용자 정보를 불러와서 UserDetails로 반환, loadUserByusername은 시큐리티 식별자로 email로 변경 안한 것
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println("사용자 정보 확인" + email);
        //DB에서 조회
        Optional<User> userData = userRepository.findByEmail(email);

        if (userData.isEmpty()) {
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다");
        }
        User user = userData.get();
        //UserDetails에 담아서 return하면 AutneticationManager가 검증 함
        return new CustomUserDetails(user);
    }
}

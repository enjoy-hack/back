package com.example.smartair.service.userService;

import com.example.smartair.dto.userDto.JoinDTO;

import com.example.smartair.repository.userRepository.UserRepository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID; // UUID 추가

@SpringBootTest
@Transactional // 각 테스트 메서드 트랜잭션, 테스트 종료시 롤백
public class JoinServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JoinService joinService;

    @Test
    void 회원가입_성공(){
        // 각 테스트 실행 시 고유한 username과 email을 생성
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
        String username = "user_" + uniqueSuffix;
        String password = "password";
        String email = "email_" + uniqueSuffix + "@example.com";


        JoinDTO joinDTO = JoinDTO.builder().username(username).password(password)
                .email(email).role("USER").build();

        Boolean success = joinService.joinProcess(joinDTO);

        Assertions.assertTrue(success, "회원가입은 성공해야 합니다."); // Assertions.assertTrue 사용 및 메시지 추가

        // 선택적: 실제로 DB에 저장되었는지, 원하는 값으로 저장되었는지 확인 (userRepository 사용)
        // com.example.smartair.entity.user.UserEntity savedUser = userRepository.findByUsername(username).orElse(null);
        // Assertions.assertNotNull(savedUser, "저장된 사용자를 찾을 수 없습니다.");
        // Assertions.assertEquals(email, savedUser.getEmail(), "이메일이 일치해야 합니다.");
        // Assertions.assertEquals(Role.USER, savedUser.getRole(), "역할이 USER여야 합니다.");
    }

}

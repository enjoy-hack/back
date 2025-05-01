package com.example.smartair.service.UserService;

import com.example.smartair.dto.userDto.JoinDTO;
import com.example.smartair.repository.userRepository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.assertThat;
@SpringBootTest
@Transactional // 각 테스트 메서드 트랜잭션, 테스트 종료시 롤백
public class JoinServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JoinService joinService;

    @Test
    void 회원가입_성공(){
        String username = "user";
        String password = "password";
        String email = "email";


        JoinDTO joinDTO = JoinDTO.builder().username(username).password(password)
                .email(email).role("USER").build();

        Boolean success = joinService.joinProcess(joinDTO);

        Assertions.assertEquals(true, success);
    }

}

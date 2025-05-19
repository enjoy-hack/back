package com.example.smartair.service.userService;

import com.example.smartair.dto.userDto.JoinDTO;
import com.example.smartair.entity.user.Role;
import com.example.smartair.entity.user.User;

import com.example.smartair.exception.CustomException;
import com.example.smartair.exception.ErrorCode;
import com.example.smartair.repository.userRepository.UserRepository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class JoinService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public JoinService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {

        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public Boolean joinProcess(JoinDTO joinDTO) {
        log.info("joinProcess 시작");
        String username = joinDTO.getUsername();
        String password = joinDTO.getPassword();
        String email = joinDTO.getEmail();
        String role = joinDTO.getRole();
        if(username == null || password == null || email == null || role == null) {
            log.info("joinProcess 실패");
            throw new CustomException(ErrorCode.USER_JOIN_INFO_BAD_REQUEST, "회원가입에 필요한 정보가 부족합니다.");

        }
        Boolean isExist = userRepository.existsByEmail(email);
        log.info("user 정보의 db 존재 확인");
        if (isExist) {
            throw new CustomException(ErrorCode.USER_ALREADY_EXISTS, "이미 존재하는 이메일입니다.");
        }

        User data = new User();

        data.setUsername(username);
        data.setPassword(bCryptPasswordEncoder.encode(password));
        data.setEmail(email);
        data.setRole(Role.valueOf(role));
        data.setLoginType("local");

        userRepository.save(data);
        return true;
    }
}

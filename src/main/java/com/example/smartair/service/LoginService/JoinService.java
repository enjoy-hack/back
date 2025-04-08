package com.example.smartair.service.LoginService;

import com.example.smartair.dto.LoginDTO.JoinDTO;
import com.example.smartair.entity.Role;
import com.example.smartair.entity.User;
import com.example.smartair.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class JoinService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public JoinService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {

        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public Boolean joinProcess(JoinDTO joinDTO) {

        String username = joinDTO.getUsername();
        String password = joinDTO.getPassword();
        String email = joinDTO.getEmail();
        String role = joinDTO.getRole();

        Boolean isExist = userRepository.existsByUsername(username);

        if (isExist) {
            return false;
        }

        User data = new User();

        data.setUsername(username);
        data.setPassword(bCryptPasswordEncoder.encode(password));
        data.setEmail(email);
        data.setRole(Role.valueOf(role));

        userRepository.save(data);
        return true;
    }
}

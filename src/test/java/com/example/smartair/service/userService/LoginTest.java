package com.example.smartair.service.userService;

import com.example.smartair.entity.user.User;

import com.example.smartair.repository.userRepository.UserRepository;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class LoginTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp(){
        User user = new User();

        user.setUsername("test");
        user.setPassword(passwordEncoder.encode("password"));
        user.setEmail("email");
        userRepository.save(user);
    }

    @Test
    void 로그인_성공_후_JWT_토큰_발급() throws Exception{
        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {

                        "email":"email",        
                        "password" : "password"
                    }
                        """))
                .andExpect(status().isOk())
                .andExpect(header().exists("access"))
                .andExpect(header().stringValues("Set-Cookie", Matchers.hasItem(Matchers.containsString("refresh="))));
    }

    @Test
    void 로그인_실패_잘못된비밀번호() throws Exception{
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "email": "testuser",
                      "password": "wrongpassword"
                    }
                """))
                .andExpect(status().isUnauthorized());
    }
}

package com.example.smartair.service.UserService;

import com.example.smartair.entity.user.User;
<<<<<<< HEAD
=======
import com.example.smartair.repository.userRepository.UserRepository;
>>>>>>> 6c51114c1b420127a6bc8f4f6a49ae80ae865b2c
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
<<<<<<< HEAD
        user.setUsername("name");
        user.setPassword(passwordEncoder.encode("password"));
        user.setEmail("test");
=======
        user.setUsername("test");
        user.setPassword(passwordEncoder.encode("password"));
        user.setEmail("email");
>>>>>>> 6c51114c1b420127a6bc8f4f6a49ae80ae865b2c
        userRepository.save(user);
    }

    @Test
    void 로그인_성공_후_JWT_토큰_발급() throws Exception{
        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
<<<<<<< HEAD
                        "email":"test",        
=======
                        "username":"test",        
>>>>>>> 6c51114c1b420127a6bc8f4f6a49ae80ae865b2c
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
<<<<<<< HEAD
                      "email": "testuser",
=======
                      "username": "testuser",
>>>>>>> 6c51114c1b420127a6bc8f4f6a49ae80ae865b2c
                      "password": "wrongpassword"
                    }
                """))
                .andExpect(status().isUnauthorized());
    }
}

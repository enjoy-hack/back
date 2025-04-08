package com.example.smartair.dto.LoginDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JoinDTO {
    private String username;
    private String password;
    private String email;
    private String role; // 입력 예시 : ROLE_ADMIN, ROLE_USER

}

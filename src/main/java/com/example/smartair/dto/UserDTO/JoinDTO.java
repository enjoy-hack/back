package com.example.smartair.dto.UserDTO;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class JoinDTO {
    private String username;
    private String password;
    private String email;
    private String role; // 입력 예시 : ROLE_ADMIN, ROLE_USER

}

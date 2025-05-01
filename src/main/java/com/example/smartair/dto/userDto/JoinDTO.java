package com.example.smartair.dto.userDto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JoinDTO {
    private String username;
    private String password;
    private String email;
    private String role; // 입력 예시 : ROLE_ADMIN, ROLE_USER

}

package com.example.smartair.dto.userDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserInfoDTO {
    private Long id;
    private String username;
    private String email;
    private String role;
    private String loginType;
}

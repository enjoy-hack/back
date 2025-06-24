package com.example.smartair.dto.userDto;

import com.example.smartair.entity.user.Role;
import com.example.smartair.entity.user.User;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class UserDetailResponseDto {
    private Long id;
    private String username;
    private String email;
    private Role role;
    private String loginType;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private int participatedRoomCount;

    public static UserDetailResponseDto from(User user) {
        return UserDetailResponseDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .loginType(user.getLoginType()) 
                .createdAt(user.getCreateDate())
                .modifiedAt(user.getModifiedDate())
                .build();
    }
} 
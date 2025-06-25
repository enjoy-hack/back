package com.example.enjoy.dto.loginDto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class MemberDto {
    private String major;
    private String studentIdString;
    private String studentName;
    private String grade;
    private String completedSemester;
    private boolean hasLoginHistory;  // 로그인 이력 여부
}

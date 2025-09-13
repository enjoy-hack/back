package com.example.enjoy.dto.loginDto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class SejongMemberInfo {
    private String major;
    private String name;
    private String studentId;
    private String grade;
    private String status;
    private String completedSemester;
}

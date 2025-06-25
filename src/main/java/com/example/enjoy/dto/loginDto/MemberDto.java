package com.example.enjoy.dto.loginDto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class MemberDto {
    private String major;
    private String studentIdString;
    private String studentName;
    private String academicYear;
    private String enrollmentStatus;

}

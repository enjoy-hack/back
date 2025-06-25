package com.example.enjoy.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 과목 정보 DTO
 * (title, year, semester, code, status)
 */
@Getter
@Setter
public class CourseStatusDto {

    private String title;    // 과목명 (기존 courseName)
    private String year;     // 이수 학년 (예: "전학년")
    private String semester; // 이수 학기 (예: "전학기")
    private String code;     // 과목 코드 (기존 courseCode)
    private String status;   // 이수 상태 ("COMPLETED", "SCHEDULED", "NONE")
}

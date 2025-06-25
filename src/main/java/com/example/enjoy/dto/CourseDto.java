package com.example.enjoy.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * 교과목 정보를 담는 DTO
 */
@Getter
@Setter
@AllArgsConstructor // 모든 필드를 인자로 받는 생성자 자동 생성
public class CourseDto {

    private String courseName;  // 현재 과목명
    private String courseAlias; // 과거 과목명 (없으면 null)
}
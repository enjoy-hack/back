package com.example.enjoy.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ParsedCourseDto {
    private String courseName;
    private StudentCourseStatus status;
}

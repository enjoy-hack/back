package com.example.enjoy.dto;

import com.example.enjoy.entity.StudentCourse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "학생 수강 과목 응답")
public class StudentCourseResponse {

    @Schema(description = "과목명")
    private String courseName;

    @Schema(description = "이수 상태")
    private StudentCourseStatus status;

    public StudentCourseResponse(String courseName, StudentCourseStatus status) {
        this.courseName = courseName;
        this.status = status;
    }

    public static StudentCourseResponse from(StudentCourse entity) {
        return new StudentCourseResponse(
                entity.getCourseName(),
                entity.getStatus()
        );
    }
}
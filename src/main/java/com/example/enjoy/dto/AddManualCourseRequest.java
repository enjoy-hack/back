package com.example.enjoy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddManualCourseRequest {

    @NotBlank(message = "학번은 필수입니다.")
    private String studentId;

    @NotBlank(message = "과목명은 필수입니다.")
    private String courseName;

    @NotNull(message = "수강 상태는 필수입니다.")
    private StudentCourseStatus status;
}
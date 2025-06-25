package com.example.enjoy.dto;

import com.example.enjoy.entity.FavoriteCourse;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FavoriteCourseDto {
    private Long id;
    private String courseName;
    private String studentId;

    public static FavoriteCourseDto from(FavoriteCourse favoriteCourse) {
        return new FavoriteCourseDto(
                favoriteCourse.getId(),
                favoriteCourse.getCourseName(),
                favoriteCourse.getUser().getStudentId()
        );
    }
}
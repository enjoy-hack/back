package com.example.enjoy.dto;

public enum StudentCourseStatus {
    COMPLETED("완료"),
    IN_PROGRESS("수강중"),
    PLANNED("수강예정"),
    FAILED("미이수");

    private final String description;

    StudentCourseStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
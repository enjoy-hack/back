package com.example.enjoy.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

/**
 * 하나의 트랙에 대한 학생의 이수 진행 현황 정보를 담는 DTO
 */
@Getter
@Setter
@AllArgsConstructor
public class TrackProgressDto {

    private String trackName;           // 트랙 이름 (예: "AI 콘텐츠")
    private String department;          // 소속 학과
    private int completedCount;         // 이수한 과목 수
    private int requiredCount;          // 이수 필요 과목 수 (예: 6)
    private boolean isCompleted;        // 트랙 이수 완료 여부

    private List<CourseDto> completedCourses; // 이수한 과목 목록
    private List<CourseDto> remainingCourses; // 이수해야 할 남은 과목 목록

    private boolean hasUploadedHistory;         // 트랙 업로드 여부
}

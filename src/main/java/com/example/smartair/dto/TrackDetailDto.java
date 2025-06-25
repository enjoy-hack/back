package com.example.smartair.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

/**
 * 상세 UI 화면의 트랙 탭 하나의 전체 정보를 담는 DTO
 * 새로 정의된 CourseStatusDto를 사용
 */
@Getter
@Setter
public class TrackDetailDto {

    private String trackName;           // 트랙 이름
    private int completedCount;         // 이수한 과목 수
    private int requiredCount = 6;      // 이수 필요 과목 수

    // 리스트의 타입이 새로운 CourseStatusDto로 변경
    private List<CourseStatusDto> courses;
}

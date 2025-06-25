package com.example.enjoy.dto;

import com.example.enjoy.entity.Track;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

/**
 * 상세 UI 화면의 트랙 탭 하나의 전체 정보를 담는 DTO
 */

@Data // 또는 @Getter, @Setter 등
public class TrackDetailDto {
    private Long trackId;
    private String trackName;
    private String department;
    private String description; // 트랙에 대한 설명 추가
    private List<CourseStatusDto> courses; // 트랙에 포함된 과목 목록

    public TrackDetailDto from (Track track) {
        this.trackId = track.getId();
        this.trackName = track.getName();
        this.department = track.getDepartment();
        return this;
    }
}

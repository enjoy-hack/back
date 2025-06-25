package com.example.enjoy.service;

import com.example.enjoy.dto.CourseDto;
import com.example.enjoy.dto.TrackProgressDto;
import com.example.enjoy.entity.StudentCourse;
import com.example.enjoy.entity.Track;
import com.example.enjoy.entity.TrackCourse;
import com.example.enjoy.repository.StudentCourseRepository;
import com.example.enjoy.repository.TrackRepository;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrackService {

    private final TrackRepository trackRepository;
    private final StudentCourseRepository studentCourseRepository; // 기존 기능

    // ... (기존의 saveStudentCoursesFromExcel, calculateTrackProgress 메서드) ...

    /**
     * 모든 트랙 정보를 학과별로 그룹화하여 반환하는 메서드
     */
    public Map<String, List<Track>> getAllTracksGroupedByDepartment() {
        // 1. DB에서 모든 트랙과 관련 과목들을 한번에 조회
        List<Track> allTracks = trackRepository.findAllWithCourses();

        // 2. 조회된 트랙 리스트를 '학과' 이름으로 그룹화하여 Map으로 변환 후 반환
        return allTracks.stream()
                .collect(Collectors.groupingBy(Track::getDepartment));
    }

    public List<TrackProgressDto> calculateTrackProgress(Long studentId) {
        // 1. 학생의 이수 과목 목록 조회
        Set<String> completedCourseNames = studentCourseRepository.findByStudentId(studentId)
                .stream()
                .map(StudentCourse::getCourseName)
                .collect(Collectors.toSet());

        // 2. 모든 트랙 정보 조회
        List<Track> allTracks = trackRepository.findAllWithCourses();

        List<TrackProgressDto> progressList = new ArrayList<>();

        // 3. 각 트랙별로 진행 현황 계산
        for (Track track : allTracks) {

            // 현재 트랙에서 완료한 과목과 남은 과목을 담을 리스트 초기화
            List<CourseDto> completedInThisTrack = new ArrayList<>();
            List<CourseDto> remainingInThisTrack = new ArrayList<>();

            // 현재 트랙에 속한 모든 교과목을 하나씩 확인
            for (TrackCourse trackCourse : track.getCourses()) {

                // 학생이 해당 과목을 이수했는지 확인 (현재 이름 또는 과거 이름으로 체크)
                if (completedCourseNames.contains(trackCourse.getCourseName()) ||
                        (trackCourse.getCourseAlias() != null && completedCourseNames.contains(trackCourse.getCourseAlias()))) {
                    // 이수한 경우: 완료 리스트에 추가
                    completedInThisTrack.add(new CourseDto(trackCourse.getCourseName(), trackCourse.getCourseAlias()));
                } else {
                    // 이수하지 않은 경우: 남은 과목 리스트에 추가
                    remainingInThisTrack.add(new CourseDto(trackCourse.getCourseName(), trackCourse.getCourseAlias()));
                }
            }

            // 최종 결과를 담을 DTO 객체 생성 및 데이터 세팅
            TrackProgressDto progressDto = new TrackProgressDto();
            progressDto.setTrackName(track.getName());
            progressDto.setDepartment(track.getDepartment());

            int completedCount = completedInThisTrack.size();
            progressDto.setCompletedCount(completedCount);
            progressDto.setRequiredCount(6); // 트랙 이수 요구 과목 수는 6개
            progressDto.setCompleted(completedCount >= 6); // 6개 이상이면 true

            progressDto.setCompletedCourses(completedInThisTrack);
            progressDto.setRemainingCourses(remainingInThisTrack);

            // 완성된 DTO를 최종 결과 리스트에 추가
            progressList.add(progressDto);
        }

        return progressList;
    }
}

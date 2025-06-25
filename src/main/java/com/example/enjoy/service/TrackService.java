package com.example.enjoy.service;

import com.example.enjoy.dto.CourseDto;
import com.example.enjoy.dto.CourseStatusDto;
import com.example.enjoy.dto.TrackDetailDto;
import com.example.enjoy.dto.TrackProgressDto;
import com.example.enjoy.entity.StudentCourse;
import com.example.enjoy.entity.Track;
import com.example.enjoy.entity.TrackCourse;
import com.example.enjoy.repository.StudentCourseRepository;
import com.example.enjoy.repository.TrackRepository;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * 학생이 이수한 과목 이름을 Set으로 반환하는 private 메서드
     */
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

    /**
     * 학생이 이수한 과목 이름을 Set으로 반환하는 메서드
     */
    @Transactional(readOnly = true)
    public TrackDetailDto getTrackDetails(Long studentId, Long trackId) {

        // 1. [리팩토링] 학생 이수 과목 조회 로직을 private 메서드로 호출
        Set<String> completedCourseNames = getCompletedCourseNames(studentId);

        // 2. ID로 트랙 정보와 소속 과목들을 한번에 조회
        Track track = trackRepository.findByIdWithCourses(trackId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 트랙입니다."));

        // 3. 트랙의 과목 목록을 CourseStatusDto 리스트로 변환
        List<CourseStatusDto> courseStatusList = track.getCourses().stream()
                .map(trackCourse -> {
                    // 4. [수정] DTO 객체 생성 및 실제 필드에 맞게 데이터 세팅
                    CourseStatusDto dto = new CourseStatusDto();
                    dto.setTitle(trackCourse.getCourseName());
                    // (TrackCourse 엔티티에 getCourseCode, getYear, getSemester가 있다고 가정합니다)
                    dto.setCode(trackCourse.getCourseCode());
                    dto.setYear(trackCourse.getAcademicYear());
                    dto.setSemester(trackCourse.getAcademicSemester());

                    // 5. [수정 & 리팩토링] 이수 여부를 판단하고, 그 결과에 따라 status(String) 값을 세팅
                    if (isCourseCompleted(trackCourse, completedCourseNames)) {
                        dto.setStatus("COMPLETED");
                    } else {
                        dto.setStatus("NONE");
                    }

                    return dto;
                })
                .collect(Collectors.toList());

        // 6. 최종적으로 TrackDetailDto를 조립하여 반환
        TrackDetailDto trackDetailDto = new TrackDetailDto();
        trackDetailDto.setTrackId(track.getId());
        trackDetailDto.setTrackName(track.getName());
        trackDetailDto.setDepartment(track.getDepartment());
        trackDetailDto.setCourses(courseStatusList);

        return trackDetailDto;
    }

    /**
     * 학생 ID로 해당 학생이 이수한 모든 과목명을 조회합니다.
     */
    private Set<String> getCompletedCourseNames(Long studentId) {
        return studentCourseRepository.findByStudentId(studentId)
                .stream()
                .map(StudentCourse::getCourseName)
                .collect(Collectors.toSet());
    }

    /**
     * 특정 과목이 이수 완료되었는지 확인합니다. (과목 별칭 포함)
     */
    private boolean isCourseCompleted(TrackCourse course, Set<String> completedCourseNames) {
        return completedCourseNames.contains(course.getCourseName()) ||
                (course.getCourseAlias() != null && completedCourseNames.contains(course.getCourseAlias()));
    }
}

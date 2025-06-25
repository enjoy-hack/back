package com.example.enjoy.service;

import ch.qos.logback.core.joran.sanity.Pair;
import com.example.enjoy.dto.CourseDto;
import com.example.enjoy.dto.CourseStatusDto;
import com.example.enjoy.dto.TrackDetailDto;
import com.example.enjoy.dto.TrackProgressDto;
import com.example.enjoy.entity.FavoriteCourse;
import com.example.enjoy.entity.StudentCourse;
import com.example.enjoy.entity.Track;
import com.example.enjoy.entity.TrackCourse;
import com.example.enjoy.entity.user.User;
import com.example.enjoy.repository.FavoriteCourseRepository;
import com.example.enjoy.repository.StudentCourseRepository;
import com.example.enjoy.repository.TrackRepository;
import com.example.enjoy.repository.UserRepository;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrackService {

    private final TrackRepository trackRepository;
    private final StudentCourseRepository studentCourseRepository; // 기존 기능
    private final FavoriteCourseRepository favoriteCourseRepository;
    private final UserRepository userRepository;

    //진척률 계산
    public List<TrackProgressDto> calculateTrackProgress(String studentId) {
        Set<String> completedCourseNames = getCompletedCourseNames(studentId); // 이수 과목명 목록

        List<Track> allTracks = trackRepository.findAll();

        return allTracks.stream().map(track -> {
            List<TrackCourse> courses = track.getCourses(); // 이 트랙의 모든 과목
            List<CourseDto> completed = new ArrayList<>();
            List<CourseDto> remaining = new ArrayList<>();

            for (TrackCourse course : courses) {
                CourseDto dto = new CourseDto(course.getCourseName(), course.getCourseAlias());
                if (isCourseCompleted(course, completedCourseNames)) {
                    completed.add(dto);
                } else {
                    remaining.add(dto);
                }
            }

            return new TrackProgressDto(
                    track.getName(),
                    track.getDepartment(),
                    completed.size(),
                    courses.size(),
                    completed.size() == courses.size(),
                    completed,
                    remaining
            );
        }).toList();
    }

    /**
     * 학생이 이수한 과목 이름을 Set으로 반환하는 메서드
     */
    @Transactional(readOnly = true)
    public TrackDetailDto getTrackDetails(String studentId, String trackName) {

        // 1. [리팩토링] 학생 이수 과목 조회 로직을 private 메서드로 호출
        Set<String> completedCourseNames = getCompletedCourseNames(studentId);

        // 2. 트랙 정보와 소속 과목들을 한번에 조회
        Track track = trackRepository.findByName(trackName)
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

    public TrackDetailDto getTrackDetailsByName(String trackName) {
        Track track = trackRepository.findByName(trackName)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 트랙입니다."));

        // 트랙의 과목 목록을 CourseStatusDto 리스트로 변환
        List<CourseStatusDto> courseStatusList = track.getCourses().stream()
                .map(trackCourse -> {
                    CourseStatusDto dto = new CourseStatusDto();
                    dto.setTitle(trackCourse.getCourseName());
                    dto.setCode(trackCourse.getCourseCode());
                    dto.setYear(trackCourse.getAcademicYear());
                    dto.setSemester(trackCourse.getAcademicSemester());
                    dto.setStatus("NONE"); // 기본값 설정, 이수 여부는 별도로 처리
                    return dto;
                })
                .collect(Collectors.toList());

        // 최종적으로 TrackDetailDto를 조립하여 반환
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
    private Set<String> getCompletedCourseNames(String studentId) {
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

    public Track getTopTrackByProgressScore(String studentId) {
        User user = userRepository.findByStudentId(studentId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        List<TrackProgressDto> trackProgress = calculateTrackProgress(studentId);

        TrackProgressDto topProgress = trackProgress.stream()
                .max((a, b) -> Double.compare(
                        (double) a.getCompletedCount() / a.getRequiredCount(),
                        (double) b.getCompletedCount() / b.getRequiredCount()))
                .orElse(null);

        if (topProgress == null) {
            return null;
        }

        return trackRepository.findByName(topProgress.getTrackName())
                .orElse(null);
    }

    // 선호과목 기준 추천 트랙 1개 반환
    public Track getTopTrackByFavoriteScore(String studentId) {
        User user = userRepository.findByStudentId(studentId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        Set<String> favoriteCourses = favoriteCourseRepository.findAllByUser(user)
                .stream()
                .map(FavoriteCourse::getCourseName)
                .collect(Collectors.toSet());

        List<TrackProgressDto> trackProgress = calculateTrackProgress(studentId);

        TrackProgressDto topFavorite = trackProgress.stream()
                .max((a, b) -> Double.compare(
                        calculateFavoriteScore(a, favoriteCourses),
                        calculateFavoriteScore(b, favoriteCourses)))
                .orElse(null);

        if (topFavorite == null) {
            return null;
        }

        return trackRepository.findByName(topFavorite.getTrackName())
                .orElse(null);
    }

    private double calculateFavoriteScore(TrackProgressDto track, Set<String> favoriteCourses) {
        List<CourseDto> allCourses = new ArrayList<>();
        allCourses.addAll(track.getCompletedCourses());
        allCourses.addAll(track.getRemainingCourses());

        long matchCount = allCourses.stream()
                .filter(course ->
                        (course.getCourseName() != null && favoriteCourses.contains(course.getCourseName())) ||
                                (course.getCourseAlias() != null && favoriteCourses.contains(course.getCourseAlias()))
                )
                .count();

        return allCourses.isEmpty() ? 0.0 : (double) matchCount / allCourses.size();
    }

    public List<Track> getAllTracks() {
        return trackRepository.findAll();
    }
}

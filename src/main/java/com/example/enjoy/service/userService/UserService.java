package com.example.enjoy.service.userService;

import com.amazonaws.services.cloudformation.model.AlreadyExistsException;
import com.example.enjoy.dto.AddManualCourseRequest;
import com.example.enjoy.dto.StudentCourseStatus;
import com.example.enjoy.entity.StudentCourse;
import com.example.enjoy.entity.Track;
import com.example.enjoy.entity.TrackCourse;
import com.example.enjoy.repository.StudentCourseRepository;
import com.example.enjoy.repository.TrackCourseRepository;
import com.example.enjoy.repository.TrackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final StudentCourseRepository studentCourseRepository;
    private final TrackRepository trackRepository;
    private final TrackCourseRepository trackCourseRepository;

    @Transactional
    public void addManualCourse(AddManualCourseRequest request) { //수동으로 과목 등록
        if (studentCourseRepository.existsByStudentIdAndCourseName(
                request.getStudentId(),
                request.getCourseName())) {
            throw new AlreadyExistsException("이미 등록된 과목입니다.");
        }

        StudentCourse sc = StudentCourse.builder() //수강 여부 설정 가능
                .studentId(request.getStudentId())
                .courseName(request.getCourseName())
                .status(request.getStatus())
                .manual(true)
                .build();
        studentCourseRepository.save(sc);
    }


    @Transactional
    public void removeManualCourse(String studentId, String courseName) { //수동 등록 과목 삭제
        StudentCourse course = studentCourseRepository.findByStudentIdAndCourseNameAndManualIsTrue(studentId, courseName)
                .orElseThrow(() -> new IllegalArgumentException("수동 등록된 과목을 찾을 수 없습니다."));
        studentCourseRepository.delete(course);
    }

    public List<StudentCourse> getCompletedCourses(String studentId) { //수강 완료 과목 조회
        return studentCourseRepository.findAllByStudentIdAndStatus(studentId, StudentCourseStatus.COMPLETED);
    }

    public Map<Track, Double> getTrackProgress(String studentId) { //트랙별 진행률 조회
        List<Track> allTracks = trackRepository.findAll();
        List<StudentCourse> completedCourses = getCompletedCourses(studentId);

        return allTracks.stream().collect(Collectors.toMap(
                track -> track,
                track -> calculateTrackProgress(track, completedCourses)
        ));
    }

    private double calculateTrackProgress(Track track, List<StudentCourse> completedCourses) {
        List<TrackCourse> trackCourses = trackCourseRepository.findAllByTrack(track);
        if (trackCourses.isEmpty()) return 0.0;

        long completedCount = trackCourses.stream()
                .filter(trackCourse -> completedCourses.stream()
                        .anyMatch(completed -> completed.getCourseName().equals(trackCourse.getCourseName())))
                .count();

        return (double) completedCount / trackCourses.size() * 100;
    }

    @Transactional
    public void updateCourseStatus(String studentId, String courseName, StudentCourseStatus newStatus) {
        StudentCourse course = studentCourseRepository
                .findByStudentIdAndCourseName(studentId, courseName)
                .orElseThrow(() -> new IllegalArgumentException("등록된 과목을 찾을 수 없습니다."));

        course.updateStatus(newStatus);
    }
}
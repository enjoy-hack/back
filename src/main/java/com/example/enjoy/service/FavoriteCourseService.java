package com.example.enjoy.service;

import com.example.enjoy.entity.FavoriteCourse;
import com.example.enjoy.entity.TrackCourse;
import com.example.enjoy.entity.user.User;
import com.example.enjoy.repository.FavoriteCourseRepository;
import com.example.enjoy.repository.TrackCourseRepository;
import com.example.enjoy.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FavoriteCourseService {
    private final FavoriteCourseRepository favoriteCourseRepository;
    private final TrackCourseRepository trackCourseRepository;
    private final UserRepository userRepository;

    @Transactional
    public void addFavoriteCourse(String studentId, String courseName) {
        User user = userRepository.findByStudentId(studentId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));


        TrackCourse trackCourse = trackCourseRepository.findByCourseName(courseName)
                .orElseThrow(() -> new RuntimeException("과목을 찾을 수 없습니다."));

        // 이미 즐겨찾기한 과목인지 확인
        boolean alreadyExists = favoriteCourseRepository
                .findByUserAndCourseName(user, courseName)
                .isPresent();

        if (!alreadyExists) {
            FavoriteCourse favoriteCourse = new FavoriteCourse(user, trackCourse.getCourseName());
            favoriteCourseRepository.save(favoriteCourse);
        }
    }

    public List<FavoriteCourse> getFavoriteCourses(String studentId) {
        User user = userRepository.findByStudentId(studentId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        return favoriteCourseRepository.findAllByUser(user);
    }
}

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

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FavoriteCourseService {
    private final FavoriteCourseRepository favoriteCourseRepository;
    private final TrackCourseRepository trackCourseRepository;
    private final UserRepository userRepository;

    @Transactional
    public void toggleFavoriteCourse(Long userId, Long trackCourseId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        TrackCourse trackCourse = trackCourseRepository.findById(trackCourseId)
                .orElseThrow(() -> new RuntimeException("과목을 찾을 수 없습니다."));

        Optional<FavoriteCourse> existingFavorite = favoriteCourseRepository
                .findByUserAndTrackCourse(user, trackCourse);

        if (existingFavorite.isPresent()) {
            favoriteCourseRepository.delete(existingFavorite.get());
        } else {
            FavoriteCourse favoriteCourse = new FavoriteCourse();
            favoriteCourse.setUser(user);
            favoriteCourse.setTrackCourse(trackCourse);
            favoriteCourseRepository.save(favoriteCourse);
        }
    }
}

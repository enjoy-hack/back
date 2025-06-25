package com.example.enjoy.controller;

import com.example.enjoy.dto.FavoriteCourseDto;
import com.example.enjoy.entity.FavoriteCourse;
import com.example.enjoy.service.FavoriteCourseService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/favorite-courses")
@RequiredArgsConstructor
public class FavoriteCourseController {
    private final FavoriteCourseService favoriteCourseService;

    @Operation(summary = "즐겨찾는 과목 추가", description = "학생의 즐겨찾는 과목을 최대 4개까지 한 번에 추가합니다.")
    @PostMapping("/add")
    public ResponseEntity<Void> addFavoriteCourses(
            @RequestParam List<String> courseNames,
            @RequestParam String studentId) {

        if (courseNames == null || courseNames.isEmpty() || courseNames.size() > 4) {
            return ResponseEntity.badRequest().build();
        }

        favoriteCourseService.addFavoriteCourses(studentId, courseNames);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "즐겨찾는 과목 조회", description = "학생의 즐겨찾는 과목을 조회합니다.")
    @GetMapping("/get")
    public ResponseEntity<List<FavoriteCourseDto>> getFavoriteCourse(
            @RequestParam String studentId) {

        List<FavoriteCourse> favoriteCourses = favoriteCourseService.getFavoriteCourses(studentId);
        if (favoriteCourses == null || favoriteCourses.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<FavoriteCourseDto> favoriteDtos = favoriteCourses.stream()
                .map(FavoriteCourseDto::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(favoriteDtos);
    }
}
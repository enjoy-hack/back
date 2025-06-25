package com.example.enjoy.controller;

import com.example.enjoy.dto.AddManualCourseRequest;
import com.example.enjoy.dto.StudentCourseResponse;
import com.example.enjoy.dto.StudentCourseStatus;
import com.example.enjoy.dto.loginDto.MemberCommand;
import com.example.enjoy.dto.loginDto.MemberDto;
import com.example.enjoy.entity.StudentCourse;
import com.example.enjoy.entity.Track;
import com.example.enjoy.service.loginService.SejongLoginService;
import com.example.enjoy.service.userService.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import okhttp3.OkHttpClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student")
public class UserController {

    private final SejongLoginService sejongLoginService;
    private final UserService userService;

    public UserController(SejongLoginService sejongLoginService, UserService userService) {
        this.sejongLoginService = sejongLoginService;
        this.userService = userService;
    }

    @Operation(summary = "학생 정보 조회", description = "세종대학교 포털 인증을 통해 학생 정보를 조회합니다.")
    @PostMapping("/detail")
    public ResponseEntity<MemberDto> getStudentDetail(@RequestBody MemberCommand command) throws IOException {
        MemberDto memberInfo = sejongLoginService.getMemberAuthInfos(command);
        return ResponseEntity.ok(memberInfo);
    }

    @Operation(summary = "수동 과목 등록", description = "학생이 직접 수강한 과목을 등록합니다.")
    @PostMapping("/courses")
    public ResponseEntity<Void> addManualCourse(@Valid @RequestBody AddManualCourseRequest request) {
        userService.addManualCourse(request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "수동 과목 조회", description = "학생이 수동으로 등록한 과목 목록을 조회합니다.")
    @GetMapping("/{studentId}/courses/manual")
    public ResponseEntity<List<StudentCourseResponse>> getManualCourses(@PathVariable String studentId) {
        List<StudentCourse> manualCourses = userService.getManualCourses(studentId);
        if (manualCourses.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(
                manualCourses.stream()
                        .map(course -> new StudentCourseResponse(course.getCourseName(), course.getStatus()))
                        .toList()
        );
    }

    @Operation(summary = "진행 예정 과목 조회", description = "학생이 수강 예정인 과목 목록을 조회합니다.")
    @GetMapping("/{studentId}/courses/planned")
    public ResponseEntity<List<StudentCourseResponse>> getPlannedCourses(@PathVariable String studentId) {
        List<StudentCourse> plannedCourses = userService.getPlannedCourses(studentId);
        if (plannedCourses.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(
                plannedCourses.stream()
                        .map(course -> new StudentCourseResponse(course.getCourseName(), course.getStatus()))
                        .toList()
        );
    }

    @Operation(summary = "수강 중인 과목 조회", description = "학생이 현재 수강 중인 과목 목록을 조회합니다.")
    @GetMapping("/{studentId}/courses/inprogress")
    public ResponseEntity<List<StudentCourseResponse>> getInProgressCourses(@PathVariable String studentId) {
        List<StudentCourse> inProgressCourses = userService.getInProgressCourses(studentId);
        if (inProgressCourses.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(
                inProgressCourses.stream()
                        .map(course -> new StudentCourseResponse(course.getCourseName(), course.getStatus()))
                        .toList()
        );
    }

    @Operation(summary = "수동 과목 삭제", description = "수동으로 등록한 과목을 삭제합니다.")
    @DeleteMapping("/courses")
    public ResponseEntity<Void> removeManualCourse(
            @RequestParam String studentId,
            @RequestParam String courseName) {
        userService.removeManualCourse(studentId, courseName);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "완료 과목 조회", description = "학생이 수강 완료한 과목 목록을 조회합니다.")
    @GetMapping("/{studentId}/courses/completed")
    public ResponseEntity<List<StudentCourse>> getCompletedCourses(@PathVariable String studentId) {
        return ResponseEntity.ok(userService.getCompletedCourses(studentId));
    }

    @Operation(summary = "트랙 진행률 조회", description = "학생의 각 트랙별 진행률을 조회합니다.")
    @GetMapping("/{studentId}/tracks/progress")
    public ResponseEntity<Map<Track, Double>> getTrackProgress(@PathVariable String studentId) {
        return ResponseEntity.ok(userService.getTrackProgress(studentId));
    }

    @Operation(summary = "과목 상태 변경", description = "등록된 과목의 수강 상태를 변경합니다.")
    @PatchMapping("/courses/status")
    public ResponseEntity<Void> updateCourseStatus(
            @RequestParam String studentId,
            @RequestParam String courseName,
            @RequestParam StudentCourseStatus newStatus) {
        userService.updateCourseStatus(studentId, courseName, newStatus);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "유저 정보 저장", description = "학생의 정보를 저장합니다.")
    @PostMapping("/save")
    public ResponseEntity<MemberDto> saveUserInfo(@Valid @RequestBody MemberDto memberDto) {
        userService.saveUserInfo(memberDto);
        return ResponseEntity.ok().build();
    }
}


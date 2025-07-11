package com.example.enjoy.controller;

import com.example.enjoy.dto.TrackProgressDto;
import com.example.enjoy.service.StudentDataService;
import com.example.enjoy.service.TrackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/student-data")
@RequiredArgsConstructor
public class StudentDataController {

    private final StudentDataService studentDataService;
    private final TrackService trackService;

    @Operation(
            summary = "엑셀 파일 업로드",
            description = "기이수 성적 엑셀 파일(.xlsx)을 업로드하고, 과목 정보를 서버에 저장합니다."
    )
    @ApiResponse(responseCode = "200", description = "업로드 성공")
    @PostMapping(value = "/upload/{studentId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadCourseExcel(
            @RequestParam("file") MultipartFile file,
            @PathVariable String studentId
    ) {
        try {
            // 1. 엑셀 파싱 및 DB 저장
            studentDataService.parseAndSaveCourses(file, studentId);

            // 2. 트랙 진행률 계산 (업로드 직후 기준)
            List<TrackProgressDto> progress = trackService.calculateTrackProgress(studentId);
            if (progress.isEmpty()) {
                return ResponseEntity.ok(Map.of("message", "현재 진행 중인 트랙이 없습니다."));
            }
            // 3. 진행률 반환
            return ResponseEntity.ok(progress);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "status", 400,
                            "code", "BAD_REQUEST",
                            "message", e.getMessage()
                    )
            );
        }
    }
}

package com.example.enjoy.service;

import com.example.enjoy.dto.ParsedCourseDto;
import com.example.enjoy.dto.StudentCourseStatus;
import com.example.enjoy.entity.StudentCourse;
import com.example.enjoy.repository.StudentCourseRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentDataService {

    private final StudentCourseRepository studentCourseRepository;

    public void parseAndSaveCourses(MultipartFile file, String studentId) {
        List<ParsedCourseDto> parsedList = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            int rowIndex = 0;
            for (Row row : sheet) {
                rowIndex++;
                if (row.getRowNum() <= 1) continue;

                Cell courseCell = row.getCell(4);
                Cell gradeCell = row.getCell(10);

                // 헤더 행 무시
                if (courseCell != null && "교과목명".equals(courseCell.getStringCellValue().trim())) {
                    continue;
                }

                // 완전 빈 행 무시
                if ((courseCell == null || courseCell.getStringCellValue().trim().isEmpty()) &&
                        (gradeCell == null || gradeCell.getStringCellValue().trim().isEmpty())) {
                    continue;
                }

                String courseName = courseCell.getStringCellValue().trim();
                String grade = gradeCell.getStringCellValue().trim();
                System.out.println(">> row " + rowIndex + ": " + courseName + " / " + grade);
                if (grade.isEmpty()) {
                    throw new RuntimeException("엑셀 파일 파싱 실패: 등급이 비어있습니다.");
                }

                StudentCourseStatus status = mapGradeToStatus(grade);

                parsedList.add(new ParsedCourseDto(courseName, status));
            }
        } catch (Exception e) {
            throw new RuntimeException("엑셀 파일 파싱 실패: " + e.getMessage(), e);
        }

        List<StudentCourse> courses = parsedList.stream()
                .map(dto -> StudentCourse.builder()
                        .studentId(studentId)
                        .courseName(dto.getCourseName())
                        .status(dto.getStatus())
                        .manual(false)
                        .createdAt(LocalDateTime.now())
                        .build())
                .toList();

        studentCourseRepository.saveAll(courses);
    }

    private StudentCourseStatus mapGradeToStatus(String grade) {
        return switch (grade.toUpperCase()) {
            case "A+", "A0", "B+", "B0", "C+", "C0", "P" -> StudentCourseStatus.COMPLETED;
            case "F", "NP" -> StudentCourseStatus.FAILED;
            default -> throw new IllegalArgumentException("잘못된 등급값: " + grade);
        };
    }

    public boolean hasUploadedHistory(String studentId) {
        return studentCourseRepository.existsByStudentId(studentId);
    }
}

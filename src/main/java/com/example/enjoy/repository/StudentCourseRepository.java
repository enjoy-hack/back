package com.example.enjoy.repository;

import com.example.enjoy.dto.StudentCourseStatus;
import com.example.enjoy.entity.StudentCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentCourseRepository extends JpaRepository<StudentCourse, Long> {

    List<StudentCourse> findByStudentId(String studentId);

    boolean existsByStudentIdAndCourseName(String studentId, String courseName);

    Optional<StudentCourse> findByStudentIdAndCourseNameAndManualIsTrue(String studentId, String courseName);

    List<StudentCourse> findAllByStudentIdAndStatus(String studentId, StudentCourseStatus status);

    Optional<StudentCourse> findByStudentIdAndCourseName(String studentId, String courseName);

    List<StudentCourse> findAllByStudentIdAndManualIsTrue(String studentId);

}
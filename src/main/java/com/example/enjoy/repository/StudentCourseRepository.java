package com.example.enjoy.repository;

import com.example.enjoy.entity.StudentCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface StudentCourseRepository extends JpaRepository<StudentCourse, Long> {

    List<StudentCourse> findByStudentId(Long studentId);

    @Transactional
    void deleteByStudentId(Long studentId); // 학생 ID로 이수과목 한번에 삭제
}
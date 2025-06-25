package com.example.enjoy.repository;

import com.example.enjoy.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByStudentId(String studentId);
    boolean existsByStudentId(String studentId);
}
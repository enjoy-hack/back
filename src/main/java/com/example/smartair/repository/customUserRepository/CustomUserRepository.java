package com.example.smartair.repository.customUserRepository;

import com.example.smartair.entity.user.CustomUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomUserRepository extends JpaRepository<CustomUser, Long> {
    Optional<CustomUser> findCustomUserByEmail(String email);
}

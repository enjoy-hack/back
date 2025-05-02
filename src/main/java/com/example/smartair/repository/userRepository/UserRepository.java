package com.example.smartair.repository.userRepository;

import com.example.smartair.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Boolean existsByEmail(String email);
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);
}

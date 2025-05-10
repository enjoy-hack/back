package com.example.smartair.repository.deviceRepository;

import com.example.smartair.entity.device.PATEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PATRepository extends JpaRepository<PATEntity, String> {
    Optional<PATEntity> findByUserId(Long userId);

    Optional<PATEntity> findByRoomId(Long roomId);

    boolean existsByUserId(Long userId);


}

package com.example.smartair.repository.hvacRepository;

import com.example.smartair.entity.hvacSetting.PATEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.web.authentication.preauth.j2ee.J2eeBasedPreAuthenticatedWebAuthenticationDetailsSource;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Repository
public interface PATRepository extends JpaRepository<PATEntity, String> {
    Optional<PATEntity> findByUserId(Long userId);

    Optional<PATEntity> findByRoomId(Long roomId);

    boolean existsByUserId(Long userId);


}

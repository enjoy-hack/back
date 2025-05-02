package com.example.smartair.repository.deviceRepository;

import com.example.smartair.entity.device.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {
    Optional<Device> findById(Long id);
}

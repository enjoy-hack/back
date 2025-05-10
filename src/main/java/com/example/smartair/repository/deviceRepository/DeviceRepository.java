package com.example.smartair.repository.deviceRepository;

import com.example.smartair.entity.airData.airQualityData.DeviceAirQualityData;
import com.example.smartair.entity.device.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {
    Optional<Device> findById(Long id);

    @Query("SELECT das.id FROM Device das WHERE das.runningStatus = true")
    List<Long> findAllRunningDeviceIds();

    Optional<Device> findBySerialNumber(Long serialNumber);
}

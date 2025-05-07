package com.example.smartair.repository.roomDeviceRepository;

import com.example.smartair.entity.device.Device;
import com.example.smartair.entity.roomDevice.RoomDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomDeviceRepository extends JpaRepository<RoomDevice, Long> {
    Optional<RoomDevice> findByDevice(Device device);

    Optional<RoomDevice> findByDevice_SerialNumberAndRoom_Id(Long deviceSerialNumber, Long roomId);

    List<RoomDevice> findByRoomId(Long roomId);

    Optional<RoomDevice> findByDevice_SerialNumber(Long serialNumber);
}

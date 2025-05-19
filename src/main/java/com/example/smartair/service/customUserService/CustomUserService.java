package com.example.smartair.service.customUserService;

import com.example.smartair.dto.userDto.CustomResponseDTO;
import com.example.smartair.entity.room.Room;
import com.example.smartair.entity.user.User;
import com.example.smartair.exception.CustomException;
import com.example.smartair.exception.ErrorCode;
import com.example.smartair.repository.roomRepository.RoomRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class CustomUserService {
    private final RoomRepository roomRepository;

    public CustomResponseDTO getCustom(User user, Long roomId){
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new RuntimeException("Room not found"));

        CustomResponseDTO response = new CustomResponseDTO();

        response.setTemperature(room.getTemperature());
        response.setMoisture(room.getMoisture());
        return response;
    }
    public void saveOrUpdateCustomTemp(User user, Double customTemp, Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if (!room.getOwner().equals(user)) {
            throw new CustomException(ErrorCode.NO_AUTHORITY, "User does not have authority to update Temp for this room");
        }

        room.setTemperature(customTemp);
        roomRepository.save(room);
    }
    public void saveOrUpdateCustomMoi(User user, Double customMoi, Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if (!room.getOwner().equals(user)) {
            throw new CustomException(ErrorCode.NO_AUTHORITY, "User does not have authority to update Moi for this room");
        }

        room.setMoisture(customMoi);
        roomRepository.save(room);
    }


}

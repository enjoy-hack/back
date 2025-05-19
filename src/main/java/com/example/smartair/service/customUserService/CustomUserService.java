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
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new CustomException(ErrorCode.ROOM_NOT_FOUND, "roomId에 맞는 방이 없습니다."));

        CustomResponseDTO response = new CustomResponseDTO();

        response.setTemperature(room.getTemperature());
        response.setMoisture(room.getMoisture());
        return response;
    }
    public void saveOrUpdateCustomTemp(User user, Double customTemp, Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROOM_NOT_FOUND, "roomId에 맞는 방이 없습니다."));

        if (!room.getOwner().equals(user)) {
            throw new CustomException(ErrorCode.NO_AUTHORITY, "해당 사용자는 방에 대한 권한이 없습니다.");
        }

        room.setTemperature(customTemp);
        roomRepository.save(room);
    }
    public void saveOrUpdateCustomMoi(User user, Double customMoi, Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROOM_NOT_FOUND, String.format("roomId에 맞는 방이 없습니다.", roomId)));

        if (!room.getOwner().equals(user)) {
            throw new CustomException(ErrorCode.NO_AUTHORITY, "해당 사용자는 방에 대한 권한이 없습니다.");
        }

        room.setMoisture(customMoi);
        roomRepository.save(room);
    }


}

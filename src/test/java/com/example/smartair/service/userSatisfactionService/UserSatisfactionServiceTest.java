package com.example.smartair.service.userSatisfactionService;

import com.example.smartair.dto.customUserDto.UserSatisfactionDto;
import com.example.smartair.entity.airData.airQualityData.RoomAirQualityData;
import com.example.smartair.entity.airScore.airQualityScore.RoomAirQualityScore;
import com.example.smartair.entity.user.Role;
import com.example.smartair.entity.user.User;
import com.example.smartair.entity.user.UserSatisfaction;
import com.example.smartair.repository.airQualityRepository.airQualityScoreRepository.RoomAirQualityScoreRepository;
import com.example.smartair.repository.customUserRepository.UserSatisfactionRepository;
import com.example.smartair.service.airQualityService.AirQualityQueryService;
import com.example.smartair.service.customUserService.UserSatisfactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserSatisfactionServiceTest {

    @InjectMocks
    private UserSatisfactionService userSatisfactionService;

    @Mock
    private UserSatisfactionRepository userSatisfactionRepository;

    @Mock
    private RoomAirQualityScoreRepository roomAirQualityScoreRepository;

    @Mock
    private AirQualityQueryService airQualityQueryService;

    private User testUser;
    private RoomAirQualityScore score;
    private RoomAirQualityData data;

    @BeforeEach
    void setup() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setRole(Role.ADMIN);

        data = new RoomAirQualityData();
        data.setAvgTemperature(21.5);
        data.setAvgHumidity(50.0);
        data.setAvgPressure(1012.0);
        data.setAvgTvoc(100.0);
        data.setAvgEco2(800.0);
        data.setAvgRawh2(5.0);
        data.setAvgRawethanol(2.0);

        score = new RoomAirQualityScore();
        score.setId(1L);
        score.setOverallScore(87.0);
        score.setRoomAirQualityData(data);
    }

    @Test
    void setUserSatisfaction_shouldSaveSuccessfully() throws Exception {
        when(roomAirQualityScoreRepository.findFirstByRoomIdOrderByCreatedAtDesc(1L))
                .thenReturn(Optional.of(score));

        userSatisfactionService.setUserSatisfaction(testUser, 4.0, 1L);

        verify(userSatisfactionRepository, times(1)).save(any(UserSatisfaction.class));
    }

    @Test
    void setUserSatisfaction_shouldThrowExceptionIfNoScore() {
        when(roomAirQualityScoreRepository.findFirstByRoomIdOrderByCreatedAtDesc(1L))
                .thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> {
            userSatisfactionService.setUserSatisfaction(testUser, 4.0, 1L);
        });
    }

    @Test
    void getUserSatisfaction_shouldReturnDtoList() throws Exception {
        UserSatisfaction us = new UserSatisfaction();
        us.setId(1L);
        us.setSatisfaction(4.5);
        us.setUserId(testUser.getId());
        us.setRoomAirQualityScore(score);
        us.setRoomAirQualityData(data);

        when(userSatisfactionRepository.findTop7ByRoomIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(us));

        List<UserSatisfactionDto> result = userSatisfactionService.getUserSatisfaction(testUser, 1L);

        assertEquals(1, result.size());
        assertEquals(4.5, result.get(0).getSatisfaction());
    }

    @Test
    void updateUserSatisfaction_shouldUpdateIfOwnerMatches() throws Exception {
        UserSatisfaction us = new UserSatisfaction();
        us.setId(1L);
        us.setUserId(testUser.getId());
        us.setSatisfaction(3.0);

        when(userSatisfactionRepository.findById(1L)).thenReturn(Optional.of(us));

        userSatisfactionService.updateUserSatisfaction(testUser, 1L, 4.5);

        assertEquals(4.5, us.getSatisfaction());
        verify(userSatisfactionRepository, times(1)).save(us);
    }

    @Test
    void updateUserSatisfaction_shouldThrowIfNotOwner() {
        UserSatisfaction us = new UserSatisfaction();
        us.setId(1L);
        us.setUserId(999L); // 다른 사용자

        when(userSatisfactionRepository.findById(1L)).thenReturn(Optional.of(us));

        assertThrows(Exception.class, () -> {
            userSatisfactionService.updateUserSatisfaction(testUser, 1L, 4.5);
        });
    }

    @Test
    void deleteUserSatisfaction_shouldDeleteIfOwnerMatches() throws Exception {
        UserSatisfaction us = new UserSatisfaction();
        us.setId(1L);
        us.setUserId(testUser.getId());

        when(userSatisfactionRepository.findById(1L)).thenReturn(Optional.of(us));

        userSatisfactionService.deleteUserSatisfaction(testUser, 1L);

        verify(userSatisfactionRepository, times(1)).delete(us);
    }

    @Test
    void deleteUserSatisfaction_shouldThrowIfNotOwner() {
        UserSatisfaction us = new UserSatisfaction();
        us.setId(1L);
        us.setUserId(999L);

        when(userSatisfactionRepository.findById(1L)).thenReturn(Optional.of(us));

        assertThrows(Exception.class, () -> {
            userSatisfactionService.deleteUserSatisfaction(testUser, 1L);
        });
    }
}



package com.example.smartair.service.deviceService;

import com.example.smartair.controller.deviceController.ThinQController;
import com.example.smartair.dto.deviceDto.DeviceReqeustDto;
import com.example.smartair.entity.login.CustomUserDetails;
import com.example.smartair.entity.user.User;
import com.example.smartair.repository.deviceRepository.PATRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ThinQControllerTest {

    @Mock
    private ThinQService thinQService;

    @Mock
    private PATRepository patRepository;

    @InjectMocks
    private ThinQController thinQController;

    private CustomUserDetails mockUserDetails;
    private User mockUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockUser = new User();
        mockUser.setId(1L);

        mockUserDetails = mock(CustomUserDetails.class);
        when(mockUserDetails.getUser()).thenReturn(mockUser);
    }

    @Test
    void getDevices_success() throws Exception {
        var dto = new DeviceReqeustDto.getDeviceListDto(100L);
        when(patRepository.existsByUserId(1L)).thenReturn(true);
        when(thinQService.getDeviceList(mockUser, 100L))
                .thenReturn(ResponseEntity.ok("mock-device-list"));

        var result = thinQController.getDevices(mockUserDetails, dto);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("mock-device-list", result.getBody());
    }

    @Test
    void getDeviceStatus_success() throws Exception {
        var dto = new DeviceReqeustDto.deviceRequestDto(100L, 10L);
        when(patRepository.existsByUserId(1L)).thenReturn(true);
        when(thinQService.getDeviceState(mockUser, dto))
                .thenReturn(ResponseEntity.ok("mock-device-status"));

        var result = thinQController.getDeviceStatus(mockUserDetails, dto);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("mock-device-status", result.getBody());
    }

    @Test
    void controlPower_success() throws Exception {
        var dto = new DeviceReqeustDto.deviceRequestDto(100L, 10L);
        when(patRepository.existsByUserId(1L)).thenReturn(true);
        when(thinQService.controlAirPurifierPower(mockUser, dto))
                .thenReturn(ResponseEntity.ok("mock-power-response"));

        var result = thinQController.controlPower(mockUserDetails, dto);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("mock-power-response", result.getBody());
    }
}
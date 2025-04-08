package com.example.smartair.service.airQualityService;

import com.example.smartair.dto.AirQualityDto.AirQualityPayloadDto;
import com.example.smartair.entity.airData.AirQualityData;
import com.example.smartair.entity.device.Device;
import com.example.smartair.entity.room.Room;
import com.example.smartair.entity.roomDevice.RoomDevice;
import com.example.smartair.infrastructure.RecentAirQualityDataCache;
import com.example.smartair.repository.AirQualityDataRepository;
import com.example.smartair.repository.DeviceRepository;
import com.example.smartair.repository.RoomDeviceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


class AirQualityDataServiceTest {

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private RoomDeviceRepository roomDeviceRepository;

    @Mock
    private AirQualityDataRepository airQualityDataRepository;

    @Mock
    private RecentAirQualityDataCache recentAirQualityDataCache;

    @InjectMocks
    private AirQualityDataService airQualityDataService;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("유효한 topic과 payload를 받을 때 : AirQualityData 저장 및 캐싱 성공")
    void processAirQualityData(){
        //given
        String topic = "iot/1";
        String payload = "{\"temperature\":25.5,\"humidity\":60,\"pressure\":1000,\"tvoc\":50,\"ppm\":450,\"rawh2\":10,\"rawethanol\":5}";

        Device device = Device.builder().id(1L).build();
        Room room  = Room.builder().id(1L).build();
        RoomDevice roomDevice = RoomDevice.builder()
                .id(1L)
                .room(room)
                .device(device)
                .build();

        when(deviceRepository.findById(1L)).thenReturn(Optional.of(device));
        when(roomDeviceRepository.findByDevice(device)).thenReturn(Optional.of(roomDevice));
        when(airQualityDataRepository.save(any(AirQualityData.class))).thenAnswer(invocation -> invocation.getArgument(0));

        //when
        AirQualityPayloadDto data = airQualityDataService.processAirQualityData(topic, payload);

        //then
        assertNotNull(data);
        assertEquals(25.5, data.getTemperature());
        assertEquals(60, data.getHumidity());
        verify(airQualityDataRepository).save(any(AirQualityData.class));
        verify(recentAirQualityDataCache).put(eq(device.getId()), any(AirQualityData.class));
    }

    @Test
    @DisplayName("디바이스가 존재하지 않을 때 : 예외 발생 및 null 반환")
    void processAirQualityData_DeviceNotFound_ShouldReturnNull(){
        //given
        String topic = "iot/1";
        String payload = "{\"temperature\":25.5,\"humidity\":60,\"pressure\":1000,\"tvoc\":50,\"ppm\":450,\"rawh2\":10,\"rawethanol\":5}";

        when(deviceRepository.findById(1L)).thenReturn(Optional.empty());

        //when
        AirQualityPayloadDto data = airQualityDataService.processAirQualityData(topic, payload);

        //then
        assertNull(data);
        verify(airQualityDataRepository, never()).save(any(AirQualityData.class));
        verify(recentAirQualityDataCache, never()).put(any(), any());
    }

    @Test
    @DisplayName("방이 존재하지 않을 때 : 예외 발생 및 null 반환")
    void processAirQualityData_RoomNotFound_ShouldReturnNull(){
        //given
        String topic = "iot/1";
        String payload = "{\"temperature\":25.5,\"humidity\":60,\"pressure\":1000,\"tvoc\":50,\"ppm\":450,\"rawh2\":10,\"rawethanol\":5}";

        Device device = Device.builder().id(1L).build();

        when(roomDeviceRepository.findById(1L)).thenReturn(Optional.empty());
        when(deviceRepository.findById(1L)).thenReturn(Optional.of(device));

        //when
        AirQualityPayloadDto data = airQualityDataService.processAirQualityData(topic, payload);

        //then
        assertNull(data);
        verify(airQualityDataRepository, never()).save(any(AirQualityData.class));
        verify(recentAirQualityDataCache, never()).put(any(), any());
    }

}
package com.example.smartair.service.airQualityService;

import com.example.smartair.dto.predictedAirQualityDto.PredictedAirQualityDto;
import com.example.smartair.entity.airData.predictedAirQualityData.PredictedAirQualityData;
import com.example.smartair.entity.sensor.Sensor;
import com.example.smartair.repository.airQualityRepository.predictedAirQualityRepository.PredictedAirQualityRepository;
import com.example.smartair.repository.roomSensorRepository.RoomSensorRepository;
import com.example.smartair.repository.sensorRepository.SensorRepository;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class PredictedAirQualityService {

    private final PredictedAirQualityRepository predictedAirQualityRepository;
    private final RoomSensorRepository roomSensorRepository;

    public void getPredictedAirQuality(PredictedAirQualityDto predictedAirQualityDto) {
        Long sensorSerialNumber = predictedAirQualityDto.getSensorSerialNumber();
        String timestamp = predictedAirQualityDto.getTimestamp();
        float pm10 = predictedAirQualityDto.getPm10();
        float co2 = predictedAirQualityDto.getCo2();
        float tvoc = predictedAirQualityDto.getTvoc();

        Long roomId = roomSensorRepository.findBySensor_SerialNumber(sensorSerialNumber).get().getRoom().getId();

        PredictedAirQualityData predictedAirQualityData = PredictedAirQualityData.builder()
                .sensorSerialNumber(sensorSerialNumber)
                .roomId(roomId)
                .timestamp(timestamp)
                .pm100(pm10)
                .co2(co2)
                .tvoc(tvoc)
                .build();

        predictedAirQualityRepository.save(predictedAirQualityData);
    }

    // 예측된 공기질 데이터를 조회하는 메소드
    public List<PredictedAirQualityData> getPredictedAirQualityBySensorSerialNumber(Long sensorSerialNumber) {
        return predictedAirQualityRepository.findByRoomId(sensorSerialNumber);

    }

//    @Scheduled(cron = "0 0 0 * * *")
//    public void deleteAllData() {
//        log.info("매일 자정 데이터 삭제 작업 시작");
//        try {
//            dailyReportRepository.deleteAllInBatch();
//            log.info("모든 데이터 삭제 완료");
//        } catch (Exception e) {
//            log.error("데이터 삭제 중 오류 발생: {}", e.getMessage(), e);
//        }
//    }
}

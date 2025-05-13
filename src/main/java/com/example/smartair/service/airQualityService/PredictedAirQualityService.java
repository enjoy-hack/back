package com.example.smartair.service.airQualityService;

import com.example.smartair.dto.predictedAirQualityDto.PredictedAirQualityDto;
import com.example.smartair.entity.airData.predictedAirQualityData.PredictedAirQualityData;
import com.example.smartair.entity.sensor.Sensor;
import com.example.smartair.repository.airQualityRepository.predictedAirQualityRepository.PredictedAirQualityRepository;
import com.example.smartair.repository.roomSensorRepository.RoomSensorRepository;
import com.example.smartair.repository.sensorRepository.SensorRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class PredictedAirQualityService {

    private final PredictedAirQualityRepository predictedAirQualityRepository;
    private final RoomSensorRepository roomSensorRepository;

    public void setPredictedAirQuality(List<PredictedAirQualityDto> predictedAirQualityDtoList) {

        for (PredictedAirQualityDto dto : predictedAirQualityDtoList) {
            Long sensorSerialNumber = dto.getSensorSerialNumber();
            String timestamp = dto.getTimestamp();
            float pm10 = dto.getPm10();
            float co2 = dto.getCo2();
            float tvoc = dto.getTvoc();

            // 센서 시리얼 번호로 방 ID 조회
            Long roomId = roomSensorRepository.findBySensor_SerialNumber(sensorSerialNumber)
                    .orElseThrow(() -> new IllegalArgumentException("해당 센서를 찾을 수 없습니다."))
                    .getRoom()
                    .getId();

            PredictedAirQualityData predictedAirQualityData;
            Optional<PredictedAirQualityData> existingData = predictedAirQualityRepository.findBySensorSerialNumberAndTimestamp(sensorSerialNumber, timestamp);

            // 예측된 공기질 데이터가 이미 존재하는 경우 업데이트
            if(existingData.isPresent()) {
                predictedAirQualityData = existingData.get();

                predictedAirQualityData.setPm10(pm10);
                predictedAirQualityData.setCo2(co2);
                predictedAirQualityData.setTvoc(tvoc);
            }else { // 예측된 공기질 데이터가 존재하지 않는 경우 새로 생성
                predictedAirQualityData = PredictedAirQualityData.builder()
                        .sensorSerialNumber(sensorSerialNumber)
                        .roomId(roomId)
                        .timestamp(timestamp)
                        .pm10(pm10)
                        .co2(co2)
                        .tvoc(tvoc)
                        .build();
            }

            predictedAirQualityRepository.save(predictedAirQualityData);
        }
    }

    // 예측된 공기질 데이터를 조회하는 메소드
    public List<PredictedAirQualityData> getPredictedAirQuality(Long sensorSerialNumber) {
        return predictedAirQualityRepository.findBySensorSerialNumberOrderByTimestamp(sensorSerialNumber);
    }

    @Scheduled(cron = "23 59 59 * * *")
    public void deleteAllData() {
        log.info("매일 자정 데이터 삭제 작업 시작");
        try {
            predictedAirQualityRepository.deleteAllInBatch();
            log.info("모든 데이터 삭제 완료");
        } catch (Exception e) {
            log.error("데이터 삭제 중 오류 발생: {}", e.getMessage(), e);
        }
    }
}

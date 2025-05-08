package com.example.smartair.service.airQualityService.scheduler;

import com.example.smartair.entity.airData.airQualityData.DeviceAirQualityData;
import com.example.smartair.exception.CustomException;
import com.example.smartair.exception.ErrorCode;
import com.example.smartair.repository.airQualityDataRepository.AirQualityDataRepository;
import com.example.smartair.repository.deviceRepository.DeviceRepository;
import com.example.smartair.service.airQualityService.AirQualityScoreService;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Setter
@RequiredArgsConstructor
@Slf4j
public class AirQualityScoreCalculationScheduler {

    private final DeviceRepository deviceRepository;
    private final AirQualityDataRepository airQualityDataRepository;
    private final AirQualityScoreService airQualityScoreService;

    @Scheduled(fixedRate = 1800000) //30분 간격
    public void calculateAirQualityScoresPeriodically() {
        LocalDateTime startTime = LocalDateTime.now();
        log.info("===Started Calculating air quality score periodically at {}===", startTime);

        int processedCount = 0;
        int failedCount = 0;

        //활성 디바이스 ID 목록 조회
        List<Long> runningDeviceIDs = deviceRepository.findAllRunningDeviceIds();
        log.info("Found running devices: {}", runningDeviceIDs);

        for (Long runningDeviceID : runningDeviceIDs) {
            try{
                log.info("Processing air quality score for device ID: {}", runningDeviceID);
                calculateScoresForDevice(runningDeviceID);
                processedCount++;
            }
            catch (Exception e){
                log.error("Error processing air quality score for device ID: {}", runningDeviceID, e);
                failedCount++;
            }
        }

        LocalDateTime endTime = LocalDateTime.now();
        Duration duration = Duration.between(startTime, endTime);
        log.info("===Finished Calculating air quality score periodically at {}===", endTime);
        log.info("Duration: {}, Total devices processed: {}, Error: {}", duration, processedCount, failedCount);
    }

    public void calculateScoresForDevice(Long deviceId) {
        //최신 7개 데이터 조회
        List<DeviceAirQualityData> airQualityDataList = airQualityDataRepository.findTop7ByDeviceIdOrderByCreatedAtDesc(deviceId);

        if (airQualityDataList.isEmpty()) {
            log.warn("No data found for device ID: {}", deviceId);
            throw new CustomException(ErrorCode.DEVICE_AIR_DATA_NOT_FOUND);
        }

        for (DeviceAirQualityData airQualityData : airQualityDataList) {
            try{
                airQualityScoreService.calculateAndSaveDeviceScore(airQualityData);
                log.debug("Score calculation successful for data ID: {}", airQualityData.getId());
            }
            catch (Exception e){
                log.error("Score calculation error for data ID: {}", airQualityData.getId(), e);
            }
        }
        log.debug("===Finished calculateScoresForDevice for device ID: {}===", deviceId);
    }

}

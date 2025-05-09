package com.example.smartair.service.airQualityService.snapshot;

import com.example.smartair.entity.airData.airQualityData.DeviceAirQualityData;
import com.example.smartair.entity.airData.fineParticlesData.FineParticlesData;
import com.example.smartair.entity.airData.snapshot.HourlyDeviceAirQualitySnapshot;
import com.example.smartair.entity.airScore.airQualityScore.DeviceAirQualityScore;
import com.example.smartair.entity.device.Device;
import com.example.smartair.exception.CustomException;
import com.example.smartair.exception.ErrorCode;
import com.example.smartair.repository.airQualityRepository.airQualityDataRepository.AirQualityDataRepository;
import com.example.smartair.repository.airQualityRepository.airQualitySnapshotRepository.HourlyDeviceAirQualitySnapshotRepository;
import com.example.smartair.repository.deviceRepository.DeviceRepository;
import com.example.smartair.service.airQualityService.calculator.AirQualityCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SnapshotService {

    private final DeviceRepository deviceRepository;
    private final AirQualityDataRepository airQualityDataRepository;
    private final HourlyDeviceAirQualitySnapshotRepository snapshotRepository;
    private final AirQualityCalculator airQualityCalculator;

    /**
     * 특정 장치의 특정 시간에 대한 시간별 스냅샷을 생성합니다.
     */
    @Transactional
    public HourlyDeviceAirQualitySnapshot createHourlySnapshot(Long deviceId, LocalDateTime snapshotHourBase) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new CustomException(ErrorCode.DEVICE_NOT_FOUND));

        // 정시 기준으로 시간 설정 (예: 2023-10-28 13:00:00)
        LocalDateTime snapshotHour = snapshotHourBase.withMinute(0).withSecond(0).withNano(0);
        LocalDateTime nextHour = snapshotHour.plusHours(1);

        // 이미 해당 시간에 대한 스냅샷이 있는지 확인
        Optional<HourlyDeviceAirQualitySnapshot> existingSnapshot =
                snapshotRepository.findByDeviceAndSnapshotHour(device, snapshotHour);

        if (existingSnapshot.isPresent()) {
            log.info("Device ID: {} 의 {} 시간 스냅샷이 이미 존재합니다. 기존 스냅샷을 업데이트합니다.",
                    deviceId, snapshotHour);
            return updateHourlySnapshot(existingSnapshot.get());
        }

        // 해당 시간대의 모든 DeviceAirQualityData 조회
        List<DeviceAirQualityData> hourlyRawDataList = airQualityDataRepository
                .findByDeviceAndCreatedAtBetweenOrderByCreatedAtAsc(device, snapshotHour, nextHour);

        if (hourlyRawDataList.isEmpty()) {
            log.warn("Device ID: {} 에 대해 {} ~ {} 시간대에 데이터가 없어 스냅샷을 생성하지 않습니다.",
                    deviceId, snapshotHour, nextHour);
            throw new CustomException(ErrorCode.DEVICE_AIR_DATA_NOT_FOUND);
        }

        // 평균값 계산
        Double avgTemperature = calculateAverage(hourlyRawDataList, DeviceAirQualityData::getTemperature);
        Double avgHumidity = calculateAverage(hourlyRawDataList, DeviceAirQualityData::getHumidity);
        Integer avgPressure = calculateIntAverage(hourlyRawDataList, DeviceAirQualityData::getPressure);
        Integer avgTvoc = calculateIntAverage(hourlyRawDataList, DeviceAirQualityData::getTvoc);
        Integer avgEco2 = calculateIntAverage(hourlyRawDataList, DeviceAirQualityData::getEco2);
        // PM10, PM25 평균 계산
        Double avgPm10 = calculateAvgPm10Standard(hourlyRawDataList);
        Double avgPm25 = calculateAvgPm25Standard(hourlyRawDataList);

        // 점수 계산을 위한 대표 데이터 생성
        DeviceAirQualityData representativeData = createRepresentativeData(
                device, avgTemperature, avgHumidity, avgPressure, avgTvoc, avgEco2, avgPm10, avgPm25);

        // 모든 점수를 한 번에 계산
        DeviceAirQualityScore calculatedScores = airQualityCalculator.calculateScore(representativeData);

        // 스냅샷 생성
        HourlyDeviceAirQualitySnapshot snapshot = HourlyDeviceAirQualitySnapshot.builder()
                .device(device)
                .snapshotHour(snapshotHour)
                .hourlyAvgTemperature(avgTemperature)
                .hourlyAvgHumidity(avgHumidity)
                .hourlyAvgPressure(avgPressure)
                .hourlyAvgTvoc(avgTvoc)
                .hourlyAvgEco2(avgEco2)
                .hourlyAvgPm10(avgPm10)
                .hourlyAvgPm25(avgPm25) // 여기까지 평균 데이터 설정
                .overallScore(calculatedScores.getOverallScore()) // 여기부터 평균 점수 설정
                .pm10Score(calculatedScores.getPm10Score())
                .pm25Score(calculatedScores.getPm25Score())
                .eco2Score(calculatedScores.getEco2Score())
                .tvocScore(calculatedScores.getTvocScore())
                .build();

        return snapshotRepository.save(snapshot);
    }

    /**
     * 특정 장치의 특정 시간에 대한 시간별 스냅샷을 조회합니다.
     */
    @Transactional(readOnly = true)
    public HourlyDeviceAirQualitySnapshot getHourlySnapshot(Long deviceId, LocalDateTime snapshotHour) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new CustomException(ErrorCode.DEVICE_NOT_FOUND));

        return snapshotRepository.findByDeviceAndSnapshotHour(device, snapshotHour)
                .orElseThrow(() -> new CustomException(ErrorCode.SNAPSHOT_NOT_FOUND));
    }

    /**
     * 기존 시간별 스냅샷을 업데이트합니다.
     */
    @Transactional
    public HourlyDeviceAirQualitySnapshot updateHourlySnapshot(HourlyDeviceAirQualitySnapshot snapshot) {
        // 기존 스냅샷의 시간 범위에 해당하는 DeviceAirQualityData 리스트 조회
        List<DeviceAirQualityData> dataList = airQualityDataRepository
                .findByDeviceAndCreatedAtBetweenOrderByCreatedAtAsc(snapshot.getDevice(),
                        snapshot.getSnapshotHour(), snapshot.getSnapshotHour().plusHours(1));

        if (dataList.isEmpty()) {
            log.warn("Device ID: {} 의 {} 시간 스냅샷에 대한 데이터가 없어 업데이트하지 않습니다.",
                    snapshot.getDevice().getId(), snapshot.getSnapshotHour());
            throw new CustomException(ErrorCode.DEVICE_AIR_DATA_NOT_FOUND);
        }

        // 평균값 재계산
        Double avgTemperature = calculateAverage(dataList, DeviceAirQualityData::getTemperature);
        Double avgHumidity = calculateAverage(dataList, DeviceAirQualityData::getHumidity);
        Integer avgPressure = calculateIntAverage(dataList, DeviceAirQualityData::getPressure);
        Integer avgTvoc = calculateIntAverage(dataList, DeviceAirQualityData::getTvoc);
        Integer avgEco2 = calculateIntAverage(dataList, DeviceAirQualityData::getEco2);
        Double avgPm10 = calculateAvgPm10Standard(dataList);
        Double avgPm25 = calculateAvgPm25Standard(dataList);

        // 점수 계산을 위한 대표 데이터 생성
        DeviceAirQualityData representativeData = createRepresentativeData(
                snapshot.getDevice(), avgTemperature, avgHumidity, avgPressure, avgTvoc, avgEco2, avgPm10, avgPm25);

        // 모든 점수를 한 번에 계산
        DeviceAirQualityScore calculatedScores = airQualityCalculator.calculateScore(representativeData);

        // 스냅샷 객체 업데이트
        snapshot.setHourlyAvgTemperature(avgTemperature);
        snapshot.setHourlyAvgHumidity(avgHumidity);
        snapshot.setHourlyAvgPressure(avgPressure);
        snapshot.setHourlyAvgTvoc(avgTvoc);
        snapshot.setHourlyAvgEco2(avgEco2);
        snapshot.setHourlyAvgPm10(avgPm10);
        snapshot.setHourlyAvgPm25(avgPm25);
        snapshot.setOverallScore(calculatedScores.getOverallScore());
        snapshot.setPm10Score(calculatedScores.getPm10Score());
        snapshot.setPm25Score(calculatedScores.getPm25Score());
        snapshot.setEco2Score(calculatedScores.getEco2Score());
        snapshot.setTvocScore(calculatedScores.getTvocScore());

        // 업데이트된 스냅샷 저장 및 반환
        log.info("Device ID: {} 의 {} 시간 스냅샷 업데이트 완료",
                snapshot.getDevice().getId(), snapshot.getSnapshotHour());
        return snapshotRepository.save(snapshot);
    }

    /**
     * DeviceAirQualityData 리스트에서 Double 필드의 평균을 계산합니다.
     */
    private <T> Double calculateAverage(List<DeviceAirQualityData> dataList,
                                        java.util.function.Function<DeviceAirQualityData, Double> fieldExtractor) {

        return dataList.stream()
                .map(fieldExtractor)
                .filter(value -> value != null)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }

    /**
     * DeviceAirQualityData 리스트에서 Integer 필드의 평균을 계산합니다.
     */
    private Integer calculateIntAverage(List<DeviceAirQualityData> dataList,
                                        java.util.function.Function<DeviceAirQualityData, Integer> fieldExtractor) {
        return (int) dataList.stream()
                .map(fieldExtractor)
                .filter(value -> value != null)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);
    }

    /**
     * 점수 계산을 위한 대표 DeviceAirQualityData 객체를 생성합니다.
     */
    private DeviceAirQualityData createRepresentativeData(Device device, Double avgTemperature,
                                                          Double avgHumidity, Integer avgPressure, Integer avgTvoc, Integer avgEco2, Double avgPm10, Double avgPm25) {

        // 미세먼지 데이터 객체 생성
        FineParticlesData fineParticlesData = new FineParticlesData();
        fineParticlesData.setPm10_standard(avgPm10);
        fineParticlesData.setPm25_standard(avgPm25);

        // 대표 데이터 객체 생성
        return DeviceAirQualityData.builder()
                .device(device)
                .temperature(avgTemperature)
                .humidity(avgHumidity)
                .pressure(avgPressure)
                .tvoc(avgTvoc)
                .eco2(avgEco2)
                .fineParticlesData(fineParticlesData)
                .build();
    }

    /**
     * DeviceAirQualityData 리스트에서 PM10 평균을 계산합니다.
     */
    private Double calculateAvgPm10Standard(List<DeviceAirQualityData> dataList) {
        return dataList.stream()
                .map(data -> data.getFineParticlesData())
                .filter(fpd -> fpd != null) // null 체크 필수
                .mapToDouble(FineParticlesData::getPm10_standard)
                .average()
                .orElse(0.0);
    }

    /**
     * DeviceAirQualityData 리스트에서 PM25_standard 평균을 계산합니다.
     */
    private Double calculateAvgPm25Standard(List<DeviceAirQualityData> dataList) {
        return dataList.stream()
                .map(DeviceAirQualityData::getFineParticlesData)
                .filter(fpd -> fpd != null) // null 체크 필수
                .mapToDouble(FineParticlesData::getPm25_standard)
                .average()
                .orElse(0.0);
    }
}
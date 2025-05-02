package com.example.smartair.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청이 들어왔습니다"),
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 사용자입니다"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    DEVICE_NOT_FOUND(HttpStatus.NOT_FOUND, "기기를 찾을 수 없습니다."),
    ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "등록되지 않은 방입니다."),
    PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, "등록되지 않은 공간입니다."),
    DEVICE_AIR_DATA_NOT_FOUND(HttpStatus.NOT_FOUND, "기기의 공기질 데이터를 찾을 수 없습니다."),
    ROOM_SCORE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 방의 공기질 점수 정보를 찾을 수 없습니다."),
    PLACE_SCORE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 공간의 공기질 점수 정보를 찾을 수 없습니다."),
    DEVICE_SCORE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 기기의 공기질 점수 정보를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),
    ROOM_DEVICE_MAPPING_NOT_FOUND(HttpStatus.NOT_FOUND, "방과 디바이스의 매핑 정보를 찾을 수 없습니다."),
    
    // === AirQualityScoreCalculator 관련 오류 코드 ===
    INVALID_INPUT_DATA(HttpStatus.BAD_REQUEST, "입력 데이터가 유효하지 않습니다 (예: null 데이터)."),
    INVALID_CONCENTRATION_RANGE(HttpStatus.BAD_REQUEST, "측정된 농도 값이 유효 범위를 벗어났습니다 (음수 또는 최대 구간 초과)."),
    UNKNOWN_POLLUTANT_TYPE(HttpStatus.INTERNAL_SERVER_ERROR, "알 수 없는 오염 물질 타입으로 점수를 계산할 수 없습니다."), 
    CALCULATION_LOGIC_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "점수 계산 로직 오류가 발생했습니다 (농도 구간 매칭 실패 등)."),

    // === mqtt 관련 오류 코드 ===
    MQTT_PROCESSING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "MQTT 데이터 처리 중 오류 발생"),

    // === AirQualityScoreService 관련 오류 코드 ===

    ROOM_AIR_QUALITY_SCORE_IS_EMPTY(HttpStatus.NO_CONTENT, "Room 점수 데이터가 비어있습니다."),

    // === Satisfaction 관련 오류 코드 ===
    SATISFACTION_NOT_FOUND(HttpStatus.NOT_FOUND,"해당 만족도 정보가 존재하지 않습니다.");

    private final HttpStatus status;
    private final String message;
}

package com.example.smartair.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    INTERNAL_SERVER_ERROR(501, "서버 내부 오류가 발생했습니다: %s"),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청이 들어왔습니다"),

    // === User 관련 오류 코드 ===
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 사용자입니다"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    USER_PASSWORD_NOT_MATCH(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다."),
    USER_JOIN_INFO_BAD_REQUEST(HttpStatus.BAD_REQUEST, "사용자 회원가입 정보가 부족합니다."),


    // === AirQualityScoreCalculator 관련 오류 코드 ===,ㅋ
    DEVICE_AIR_DATA_NOT_FOUND(HttpStatus.NOT_FOUND, "기기의 공기질 데이터를 찾을 수 없습니다."),

    // === Snapshot 관련 오류 코드 ===
    SNAPSHOT_NOT_FOUND(HttpStatus.NOT_FOUND, "스냅샷을 찾을 수 없습니다."),

    // === Report 관련 오류 코드 ===
    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "보고서를 찾을 수 없습니다."),
    INVALID_DATE_RANGE(HttpStatus.BAD_REQUEST, "유효하지 않은 날짜 범위입니다."),
    NO_DAILY_REPORTS_FOUND(HttpStatus.NO_CONTENT, "해당 날짜에 대한 일일 보고서가 없습니다."),
    INSUFFICIENT_DAILY_REPORTS(HttpStatus.BAD_REQUEST, "일일 보고서가 충분하지 않습니다. 최소 1개의 보고서가 필요합니다."),

    // === Room 관련 오류 코드 ===
    INVALID_ROOM_PASSWORD(HttpStatus.UNAUTHORIZED, "방 비밀번호가 일치하지 않습니다."),
    OWNER_CANNOT_JOIN_OWN_ROOM(HttpStatus.BAD_REQUEST, "방장은 자신의 방에 참여할 수 없습니다."),
    ALREADY_PARTICIPATING_IN_ROOM(HttpStatus.CONFLICT, "이미 해당 방에 참여하고 있습니다."),
    NO_AUTHORITY_TO_MANAGE_PARTICIPANTS(HttpStatus.FORBIDDEN, "방 참여자를 관리할 권한이 없습니다."),
    PARTICIPANT_NOT_FOUND_IN_ROOM(HttpStatus.NOT_FOUND, "해당 방에서 참여자를 찾을 수 없습니다."),
    CANNOT_CHANGE_OWNER_DEVICE_CONTROL(HttpStatus.BAD_REQUEST, "방장의 기기 제어 권한은 변경할 수 없습니다."),
    CANNOT_REMOVE_OWNER_FROM_ROOM(HttpStatus.BAD_REQUEST, "방장은 강퇴시킬 수 없습니다."),
    CANNOT_REMOVE_SELF_FROM_ROOM(HttpStatus.BAD_REQUEST, "자기 자신을 강퇴시킬 수 없습니다."),
    PAT_PERMISSION_REQUEST_ALREADY_EXISTS(HttpStatus.CONFLICT, "PAT 제어 권한 요청이 이미 존재합니다."),
    REQUEST_ALREADY_PENDING(HttpStatus.CONFLICT, "요청이 이미 보류 중입니다."),
    INVALID_REQUEST_STATUS(HttpStatus.BAD_REQUEST, "요청 상태가 유효하지 않습니다."),
    INVALID_TARGET_PARTICIPANT(HttpStatus.BAD_REQUEST, "대상 참여자가 유효하지 않습니다."),
    ROOM_SENSOR_MAPPING_ALREADY_EXISTS(HttpStatus.CONFLICT, "방과 센서의 매핑 정보가 이미 존재합니다."),

    // === PAT 관련 오류 코드 ===
    PAT_NOT_FOUND(403, "PAT 정보를 찾을 수 없습니다."),
    FCM_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "FCM 토큰을 찾을 수 없습니다."),
    FCM_MESSAGE_SEND_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "FCM 메시지 전송 중 오류가 발생했습니다."),

    // === Satisfaction 관련 오류 코드 ===
    SATISFACTION_NOT_FOUND(HttpStatus.NOT_FOUND,"해당 만족도 정보가 존재하지 않습니다."),
    FILE_UPLOAD_ERROR(HttpStatus.CONFLICT, "S3 파일 업로드 중 오류가 발생하였습니다."),

    // === Device 관련 오류 코드 ===
    DEVICE_NOT_FOUND(HttpStatus.NOT_FOUND, "기기를 찾을 수 없습니다."),
    DEVICE_STATE_NOT_FOUND(HttpStatus.NOT_FOUND, "기기 상태를 찾을 수 없습니다."),
    DEVICE_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "기기 API 호출 중 오류가 발생했습니다."),;

    private final String message;
    private final int status;

    // int 생성자
    ErrorCode(int status, String message) {
        this.status = status;
        this.message = message;
    }

    // HttpStatus 생성자
    ErrorCode(HttpStatus httpStatus, String message) {
        this.status = httpStatus.value();
        this.message = message;
    }

}

package com.example.enjoy.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    INTERNAL_SERVER_ERROR(501, "서버 내부 오류가 발생했습니다: %s"),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청이 들어왔습니다"),
    USER_JOIN_INFO_BAD_REQUEST(HttpStatus.BAD_REQUEST, "회원가입 정보가 잘못되었습니다"),
    USER_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 존재하는 사용자입니다"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다"),
    USER_PASSWORD_NOT_MATCH(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다"),
    SEJONG_AUTH_CONNECTION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "세종대학교 인증 서버와의 연결에 실패했습니다"),
    SEJONG_AUTH_CREDENTIALS_INVALID(HttpStatus.UNAUTHORIZED, "세종대학교 인증 정보가 유효하지 않습니다"),
    SEJONG_AUTH_DATA_FETCH_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "세종대학교 인증 데이터 가져오기 실패");


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

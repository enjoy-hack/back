package com.example.smartair.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청이 들어왔습니다"),
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 사용자입니다");

    private final HttpStatus status;
    private final String message;
}

package com.example.enjoy.exception;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> handleResponseStatusException(ResponseStatusException e) {
        log.error("ResponseStatusException 발생: 상태코드={}, 메시지={}",
                e.getStatusCode(), e.getReason());

        return ResponseEntity
                .status(e.getStatusCode())
                .body(new HashMap<String, Object>() {{
                    put("status", e.getStatusCode().value());
                    put("message", e.getReason());
                }});
    }


    @ExceptionHandler(CustomException.class)
    public ResponseEntity<?> handleCustomException(CustomException e) {
        ErrorCode errorCode = e.getErrorCode();
        log.error("CustomException 발생: 상태코드={}, 에러코드={}, 메시지={}",
                errorCode.getStatus(), errorCode.name(), e.getMessage());

        return createErrorResponse(errorCode, e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntimeException(RuntimeException e) {
        log.error("RuntimeException 발생: {}, 타입: {}",
                e.getMessage(), e.getClass().getSimpleName());

        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        return createErrorResponse(errorCode, e.getMessage());
    }

    private ResponseEntity<?> createErrorResponse(ErrorCode errorCode, String message) {
        return ResponseEntity.status(errorCode.getStatus())
                .body(new HashMap<String, Object>() {{
                    put("status", errorCode.getStatus());
                    put("code", errorCode.name());
                    put("message", message);
                }});
    }

}

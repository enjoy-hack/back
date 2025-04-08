package com.example.smartair.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Collections;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<?> handleCustomException(CustomException e) {
        log.error("CustomException 발생: {}", e.getMessage());
        return ResponseEntity.status(e.getErrorCode().getStatus())
                .body(Collections.singletonMap("error", e.getErrorCode().getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public String handleRuntimeException() {
        log.info("RuntimeException 처리 시작");
        return "Runtime Exception 핸들링";
    }
}

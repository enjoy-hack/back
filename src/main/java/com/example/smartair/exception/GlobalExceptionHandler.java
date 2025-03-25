package com.example.smartair.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(NullPointerException.class)
    public String handleNullPointerException() {
        log.info("NullPointer Exception 처리 시작");
        return "NullPointer Exception 핸들링";
    }

    @ExceptionHandler(RuntimeException.class)
    public String handleRuntimeException() {
        log.info("RuntimeException 처리 시작");
        return "Runtime Exception 핸들링";
    }
}

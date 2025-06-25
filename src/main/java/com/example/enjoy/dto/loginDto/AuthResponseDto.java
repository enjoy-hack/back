package com.example.enjoy.dto.loginDto;

public class AuthResponseDto {
    private String msg;
    private Result result;
    private String code;
    private Boolean is_auth;
    private Integer status_code;
    private Boolean success;
    private String version;

    public static class Result {
        private String authenticator;
        private Body body;
        // getters/setters
    }

    public static class Body {
        private String major;
        private String name;
        // getters/setters
    }
    // getters/setters
}

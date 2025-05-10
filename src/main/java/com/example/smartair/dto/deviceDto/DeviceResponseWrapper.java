package com.example.smartair.dto.deviceDto;

import lombok.Data;

import java.util.List;

@Data
public class DeviceResponseWrapper {
    private List<DeviceResponse> response;

    @Data
    public static class DeviceResponse {
        private String deviceId;
        private DeviceInfo deviceInfo;
    }

    @Data
    public static class DeviceInfo {
        private String deviceType;
        private String modelName;
        private String alias;
    }
}

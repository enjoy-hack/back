package com.example.smartair.dto.deviceDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class DeviceStateResponseDto {

    @JsonProperty("messageId")
    private String messageId;

    @JsonProperty("timestamp")
    private String timestamp;

    @JsonProperty("response")
    private Response response;

    @Getter
    @Setter
    public static class Response {

        @JsonProperty("airFanJobMode")
        private AirFanJobMode airFanJobMode;

        @JsonProperty("operation")
        private Operation operation;

        @JsonProperty("timer")
        private Timer timer;

        @JsonProperty("sleepTimer")
        private SleepTimer sleepTimer;

        @JsonProperty("airFlow")
        private AirFlow airFlow;

        @JsonProperty("airQualitySensor")
        private AirQualitySensor airQualitySensor;

        @JsonProperty("display")
        private Display display;
    }

    @Getter
    @Setter
    public static class AirFanJobMode {
        @JsonProperty("currentJobMode")
        private String currentJobMode;
    }

    @Getter
    @Setter
    public static class Operation {
        @JsonProperty("airFanOperationMode")
        private String airFanOperationMode;
    }

    @Getter
    @Setter
    public static class Timer {
        @JsonProperty("absoluteStartTimer")
        private String absoluteStartTimer;

        @JsonProperty("absoluteStopTimer")
        private String absoluteStopTimer;
    }

    @Getter
    @Setter
    public static class SleepTimer {
        @JsonProperty("relativeStopTimer")
        private String relativeStopTimer;
    }

    @Getter
    @Setter
    public static class AirFlow {
        @JsonProperty("windStrength")
        private String windStrength;

        @JsonProperty("windTemperature")
        private int windTemperature;

        @JsonProperty("windAngle")
        private String windAngle;

        @JsonProperty("warmMode")
        private String warmMode;
    }

    @Getter
    @Setter
    public static class AirQualitySensor {
        @JsonProperty("odor")
        private int odor;

        @JsonProperty("odorLevel")
        private String odorLevel;

        @JsonProperty("PM1")
        private int pm1;

        @JsonProperty("PM2")
        private int pm2;

        @JsonProperty("PM10")
        private int pm10;

        @JsonProperty("humidity")
        private int humidity;

        @JsonProperty("temperature")
        private double temperature;

        @JsonProperty("totalPollution")
        private int totalPollution;

        @JsonProperty("totalPollutionLevel")
        private String totalPollutionLevel;

        @JsonProperty("monitoringEnabled")
        private String monitoringEnabled;
    }

    @Getter
    @Setter
    public static class Display {
        @JsonProperty("light")
        private String light;
    }
}
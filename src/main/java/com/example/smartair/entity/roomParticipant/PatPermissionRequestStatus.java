package com.example.smartair.entity.roomParticipant;

public enum PatPermissionRequestStatus {
    NONE ("요청 없음"),
    PENDING ("승인 대기 중"),
    APPROVED ("승인됨"),
    REJECTED ("거절됨");

    private String label;

    PatPermissionRequestStatus(String label) {
        this.label = label;
    }
}

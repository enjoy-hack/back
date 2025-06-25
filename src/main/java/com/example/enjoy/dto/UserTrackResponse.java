package com.example.enjoy.dto;

import com.example.enjoy.entity.FavoriteCourse;
import com.example.enjoy.entity.UserTrack;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserTrackResponse {
    private String studentId;
    private String trackName;

    public static UserTrackResponse from(UserTrack userTrack) {
        return new UserTrackResponse(
                userTrack.getUser().getStudentId(),
                userTrack.getTrack().getName()
        );
    }
}

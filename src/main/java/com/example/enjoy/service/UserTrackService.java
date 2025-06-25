package com.example.enjoy.service;

import com.example.enjoy.entity.Track;
import com.example.enjoy.entity.UserTrack;
import com.example.enjoy.entity.user.User;
import com.example.enjoy.repository.TrackRepository;
import com.example.enjoy.repository.UserRepository;
import com.example.enjoy.repository.UserTrackRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserTrackService {

    private final TrackRepository trackRepository;
    private final UserRepository userRepository;
    private final UserTrackRepository userTrackRepository;

    public UserTrackService(TrackRepository trackRepository, UserRepository userRepository, UserTrackRepository userTrackRepository) {
        this.trackRepository = trackRepository;
        this.userRepository = userRepository;
        this.userTrackRepository = userTrackRepository;
    }

    public UserTrack addUserTrack(String studentId, String trackName) {
        User user = userRepository.findByStudentId(studentId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Track track = trackRepository.findByName(trackName)
                .orElseThrow(() -> new RuntimeException("트랙을 찾을 수 없습니다."));

        UserTrack userTrack = new UserTrack();
        userTrack.setUser(user);
        userTrack.setTrack(track);

        userTrackRepository.save(userTrack);
        return userTrack;
    }

    public List<Track> getUserTracks(String studentId) {
        User user = userRepository.findByStudentId(studentId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        return userTrackRepository.findAllByUser(user)
                .stream()
                .map(UserTrack::getTrack)
                .collect(Collectors.toList());
    }

    // 관심 트랙 삭제
    public void removeUserTrack(String studentId, String trackName) {
        User user = userRepository.findByStudentId(studentId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Track track = trackRepository.findByName(trackName)
                .orElseThrow(() -> new RuntimeException("트랙을 찾을 수 없습니다."));

        UserTrack userTrack = userTrackRepository.findByUserAndTrack(user, track)
                .orElseThrow(() -> new RuntimeException("사용자 트랙을 찾을 수 없습니다."));

        userTrackRepository.delete(userTrack);
    }


}

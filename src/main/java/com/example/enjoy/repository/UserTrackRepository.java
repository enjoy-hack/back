package com.example.enjoy.repository;

import com.example.enjoy.entity.Track;
import com.example.enjoy.entity.UserTrack;
import com.example.enjoy.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserTrackRepository extends JpaRepository<UserTrack, Long> {

    List<UserTrack> findAllByUser(User user);

    Optional<UserTrack> findByUserAndTrack(User user, Track track);
}

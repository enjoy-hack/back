package com.example.enjoy.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class Track {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String department;

    @OneToMany(mappedBy = "track", cascade = CascadeType.ALL)
    private List<TrackCourse> courses = new ArrayList<>();

    @OneToMany(mappedBy = "track", cascade = CascadeType.ALL)
    private List<UserTrack> userTracks = new ArrayList<>();

    public void addUserTrack(UserTrack userTrack) {
        userTracks.add(userTrack);
        userTrack.setTrack(this);
    }


//    public void addCourse(TrackCourse course) {
//        courses.add(course);
//        course.setTrack(this);
//    }
}

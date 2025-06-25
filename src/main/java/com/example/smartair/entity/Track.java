package com.example.smartair.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CascadeType;
import org.springframework.data.annotation.Id;

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
//    public void addCourse(TrackCourse course) {
//        courses.add(course);
//        course.setTrack(this);
//    }
}

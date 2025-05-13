package com.example.smartair.entity.room;

// import com.example.smartair.entity.place.Place; // Place import 제거
import com.example.smartair.entity.roomParticipant.RoomParticipant;
import com.example.smartair.entity.user.User;
import com.example.smartair.util.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Room extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    private String password;

    @Column(nullable = false)
    private boolean deviceControlEnabled;

    // Place 관련 필드 및 어노테이션 삭제
    // @ManyToOne
    // @JoinColumn(name = "place_id")
    // private Place place;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<RoomParticipant> participants = new HashSet<>();

    private double latitude;
    private double longitude;

    public void addParticipant(RoomParticipant roomParticipant) { //양방향 관계 설정
        this.participants.add(roomParticipant);
        roomParticipant.setRoom(this);
    }


}

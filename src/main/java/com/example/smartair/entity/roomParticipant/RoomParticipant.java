package com.example.smartair.entity.roomParticipant;

import com.example.smartair.entity.room.Room;
import com.example.smartair.entity.user.User;
import com.example.smartair.entity.user.Role;
import com.example.smartair.util.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "room_participant", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"room_id", "user_id"})
})
public class RoomParticipant extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role roleInRoom;

    @Column(nullable = false)
    private Boolean canControlPatDevices = false;

    private PatPermissionRequestStatus patPermissionRequestStatus = PatPermissionRequestStatus.NONE;

    public RoomParticipant(Room room, User user, Role roleInRoom, Boolean canControlDevices) {
        this.room = room;
        this.user = user;
        this.roleInRoom = roleInRoom;
        this.canControlPatDevices = canControlDevices;
    }
}
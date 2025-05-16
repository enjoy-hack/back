package com.example.smartair.entity.roomParticipant;

import com.example.smartair.entity.room.Room;
import com.example.smartair.entity.user.User;
import com.example.smartair.entity.user.Role;
import com.example.smartair.util.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.checkerframework.checker.units.qual.A;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
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
    private Boolean canControlPatDevices;

    private PatPermissionRequestStatus patPermissionRequestStatus;

    public RoomParticipant(Room room, User user, Role roleInRoom, Boolean canControlDevices) {
        this.room = room;
        this.user = user;
        this.roleInRoom = roleInRoom;
        this.canControlPatDevices = canControlDevices;
    }
}
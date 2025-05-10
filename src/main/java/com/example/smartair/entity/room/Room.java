package com.example.smartair.entity.room;

import com.example.smartair.entity.place.Place;
import com.example.smartair.entity.user.User;
import com.example.smartair.util.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

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

    @ManyToOne //방과 유저 : 다대일 관계
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne //방과 공간 : 다대일 관계
    @JoinColumn(name = "place_id")
    private Place place;
}

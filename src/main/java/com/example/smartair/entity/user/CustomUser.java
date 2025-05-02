package com.example.smartair.entity.user;

import com.example.smartair.util.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

@Builder
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomUser extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private Double temperature;

    private Double moisture;
}

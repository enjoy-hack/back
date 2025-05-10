package com.example.smartair.entity.hvacSetting;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PATEntity {
    @Id
    private Long userId;

    private Long roomId;

    @Column(length = 1024)
    private String encryptedPat;

    private Boolean setting; // true: 공개키, false: 비공개키
}

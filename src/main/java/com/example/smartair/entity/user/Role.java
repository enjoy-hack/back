package com.example.smartair.entity;

import lombok.Getter;

@Getter
public enum Role {
    ADMIN("ROLE_ADMIN"),
    USER("ROLE_USER");

    Role(String value){
        this.value = value;
    }
    private String value;

}

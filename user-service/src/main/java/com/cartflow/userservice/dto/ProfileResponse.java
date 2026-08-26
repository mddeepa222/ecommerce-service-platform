package com.cartflow.userservice.dto;

import com.cartflow.userservice.entity.Profile;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ProfileResponse {

    private Long userId;
    private String name;
    private String email;
    private String phone;
    private LocalDateTime createdAt;

    public static ProfileResponse from(Profile profile, String name, String email) {
        return ProfileResponse.builder()
                .userId(profile.getUserId())
                .name(name)
                .email(email)
                .phone(profile.getPhone())
                .createdAt(profile.getCreatedAt())
                .build();
    }
}

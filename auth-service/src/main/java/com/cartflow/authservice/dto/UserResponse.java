package com.cartflow.authservice.dto;

import com.cartflow.authservice.entity.Provider;
import com.cartflow.authservice.entity.Role;
import com.cartflow.authservice.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserResponse {

    private Long id;
    private String name;
    private String email;
    private Role role;
    private Provider provider;
    private String avatarUrl;
    private LocalDateTime createdAt;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .provider(user.getProvider())
                .avatarUrl(user.getAvatarUrl())
                .createdAt(user.getCreatedAt())
                .build();
    }
}

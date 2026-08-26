package com.cartflow.authservice.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String name;
    private String password;
}

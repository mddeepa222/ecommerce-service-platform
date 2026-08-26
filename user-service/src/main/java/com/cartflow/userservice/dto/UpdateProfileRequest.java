package com.cartflow.userservice.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class UpdateProfileRequest {

    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid phone number")
    private String phone;
}

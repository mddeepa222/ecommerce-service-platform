package com.cartflow.authservice.dto;

import lombok.Data;

@Data
public class RefreshRequest {
    private String refreshToken;
}

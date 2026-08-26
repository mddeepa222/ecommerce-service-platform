package com.cartflow.authservice.controller;

import com.cartflow.authservice.dto.ApiResponse;
import com.cartflow.authservice.dto.UpdateProfileRequest;
import com.cartflow.authservice.dto.UserResponse;
import com.cartflow.authservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/auth/account")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMyProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok("Profile fetched",
                userService.getMyProfile(userDetails.getUsername())));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateMyProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Profile updated",
                userService.updateMyProfile(userDetails.getUsername(), request)));
    }

    @PostMapping("/me/avatar")
    public ResponseEntity<ApiResponse<UserResponse>> uploadAvatar(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.ok("Avatar uploaded",
                userService.uploadAvatar(userDetails.getUsername(), file)));
    }

    @DeleteMapping("/me/avatar")
    public ResponseEntity<ApiResponse<Void>> deleteAvatar(
            @AuthenticationPrincipal UserDetails userDetails) {
        userService.deleteAvatar(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Avatar removed", null));
    }

    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> deleteMyAccount(
            @AuthenticationPrincipal UserDetails userDetails) {
        userService.deleteMyAccount(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Account deleted", null));
    }
}

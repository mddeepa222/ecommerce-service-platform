package com.cartflow.authservice.controller;

import com.cartflow.authservice.dto.ApiResponse;
import com.cartflow.authservice.dto.UserResponse;
import com.cartflow.authservice.entity.Role;
import com.cartflow.authservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers(
            @RequestParam(required = false) String search) {
        List<UserResponse> users = (search != null && !search.isBlank())
                ? userService.searchUsers(search)
                : userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.ok("Users fetched", users));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("User fetched", userService.getUserById(id)));
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<ApiResponse<UserResponse>> changeRole(
            @PathVariable Long id,
            @RequestParam Role role) {
        return ResponseEntity.ok(ApiResponse.ok("Role updated", userService.changeRole(id, role)));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.ok("User deleted", null));
    }
}

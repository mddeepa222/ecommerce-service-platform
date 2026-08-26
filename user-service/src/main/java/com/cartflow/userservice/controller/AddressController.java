package com.cartflow.userservice.controller;

import com.cartflow.userservice.dto.AddressRequest;
import com.cartflow.userservice.dto.AddressResponse;
import com.cartflow.userservice.dto.ApiResponse;
import com.cartflow.userservice.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getAll(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(ApiResponse.ok("Addresses fetched", addressService.getAll(userId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AddressResponse>> add(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody AddressRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Address added", addressService.add(userId, request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AddressResponse>> update(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id,
            @Valid @RequestBody AddressRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Address updated", addressService.update(userId, id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        addressService.delete(userId, id);
        return ResponseEntity.ok(ApiResponse.ok("Address deleted", null));
    }

    @PutMapping("/{id}/default")
    public ResponseEntity<ApiResponse<AddressResponse>> setDefault(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Default address updated", addressService.setDefault(userId, id)));
    }
}

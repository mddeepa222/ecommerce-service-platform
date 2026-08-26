package com.cartflow.productservice.controller;

import com.cartflow.productservice.dto.ApiResponse;
import com.cartflow.productservice.dto.CategoryRequest;
import com.cartflow.productservice.dto.CategoryResponse;
import com.cartflow.productservice.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok("Categories fetched", categoryService.getAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Category fetched", categoryService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> create(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @Valid @RequestBody CategoryRequest request) {
        requireAdmin(role);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Category created", categoryService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> update(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {
        requireAdmin(role);
        return ResponseEntity.ok(ApiResponse.ok("Category updated", categoryService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @PathVariable Long id) {
        requireAdmin(role);
        categoryService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Category deleted", null));
    }

    private void requireAdmin(String role) {
        if (!"ADMIN".equals(role)) {
            throw new SecurityException("Admin access required");
        }
    }
}

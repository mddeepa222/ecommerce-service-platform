package com.cartflow.productservice.controller;

import com.cartflow.productservice.dto.ApiResponse;
import com.cartflow.productservice.dto.ProductRequest;
import com.cartflow.productservice.dto.ProductResponse;
import com.cartflow.productservice.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getAll(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.ok("Products fetched", productService.getAll(pageable)));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.ok("Search results", productService.search(q, pageable)));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.ok("Products fetched", productService.getByCategory(categoryId, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Product fetched", productService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> create(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @Valid @RequestBody ProductRequest request) {
        requireAdmin(role);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Product created", productService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> update(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        requireAdmin(role);
        return ResponseEntity.ok(ApiResponse.ok("Product updated", productService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @PathVariable Long id) {
        requireAdmin(role);
        productService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Product deleted", null));
    }

    private void requireAdmin(String role) {
        if (!"ADMIN".equals(role)) {
            throw new SecurityException("Admin access required");
        }
    }
}

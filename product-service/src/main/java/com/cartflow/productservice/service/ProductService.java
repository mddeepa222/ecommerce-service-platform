package com.cartflow.productservice.service;

import com.cartflow.productservice.dto.ProductRequest;
import com.cartflow.productservice.dto.ProductResponse;
import com.cartflow.productservice.entity.Category;
import com.cartflow.productservice.entity.Product;
import com.cartflow.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;

    public Page<ProductResponse> getAll(Pageable pageable) {
        return productRepository.findByIsActiveTrue(pageable)
                .map(p -> toResponse(p));
    }

    public Page<ProductResponse> getByCategory(Long categoryId, Pageable pageable) {
        categoryService.findById(categoryId);
        return productRepository.findByCategoryIdAndIsActiveTrue(categoryId, pageable)
                .map(p -> toResponse(p));
    }

    public Page<ProductResponse> search(String query, Pageable pageable) {
        return productRepository.search(query, pageable)
                .map(p -> toResponse(p));
    }

    public ProductResponse getById(Long id) {
        return toResponse(findById(id));
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        categoryService.findById(request.getCategoryId());
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .imageUrl(request.getImageUrl())
                .categoryId(request.getCategoryId())
                .build();
        return toResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = findById(id);
        categoryService.findById(request.getCategoryId());
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setImageUrl(request.getImageUrl());
        product.setCategoryId(request.getCategoryId());
        return toResponse(productRepository.save(product));
    }

    @Transactional
    public void delete(Long id) {
        Product product = findById(id);
        product.setIsActive(false);
        productRepository.save(product);
    }

    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));
    }

    private ProductResponse toResponse(Product p) {
        String categoryName = categoryService.findById(p.getCategoryId()).getName();
        return ProductResponse.from(p, categoryName);
    }
}

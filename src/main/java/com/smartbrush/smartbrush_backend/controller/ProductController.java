package com.smartbrush.smartbrush_backend.controller;

import com.smartbrush.smartbrush_backend.entity.Product;
import com.smartbrush.smartbrush_backend.repository.ProductRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
@Tag(name = "제품추천", description = "사용자별 제품 추천하기")
public class ProductController {

    private final ProductRepository productRepository;

    @Operation(summary = "제품추천", description = "category에 shampoo, conditioner, essence, tonic, treatment 중 하나를 넣어주세요.")
    @GetMapping("/{category}")
    public List<Product> getProductsByCategory(
            @PathVariable String category,
            @RequestParam(required = false) String keyword) {

        if (keyword != null && !keyword.isBlank()) {
            return productRepository.findByCategoryAndNameContainingIgnoreCase(category, keyword);
        } else {
            return productRepository.findByCategory(category);
        }
    }
}

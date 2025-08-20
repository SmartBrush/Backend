package com.smartbrush.smartbrush_backend.controller;

import com.smartbrush.smartbrush_backend.entity.Product;
import com.smartbrush.smartbrush_backend.repository.ProductRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
@Tag(name = "제품추천", description = "사용자별 제품 추천하기")
public class ProductController {

    private final ProductRepository productRepository;

    @Operation(
            summary = "제품추천",
            description = "category에 shampoo, conditioner, essence, tonic, treatment 중 하나를 넣어주세요. size로 개수 제한 가능합니다."
    )
    @GetMapping("/{category}")
    public List<Product> getProductsByCategory(
            @PathVariable String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "20") int size
    ) {
        int safeSize = Math.max(size, 1);
        Pageable pageable = PageRequest.of(0, safeSize);

        if (keyword != null && !keyword.isBlank()) {
            return productRepository
                    .findByCategoryAndNameContainingIgnoreCase(category, keyword, pageable)
                    .getContent();
        } else {
            return productRepository
                    .findByCategory(category, pageable)
                    .getContent();
        }
    }

    @Operation(summary = "모든 제품 가져오기", description = "size 파라미터로 개수 제한 가능합니다.")
    @GetMapping
    public List<Product> getAllProducts(@RequestParam(defaultValue = "20") int size) {
        int safeSize = Math.max(size, 1);
        Pageable pageable = PageRequest.of(0, safeSize);
        return productRepository.findAll(pageable).getContent();
    }
}

package com.smartbrush.smartbrush_backend.controller;

import com.smartbrush.smartbrush_backend.entity.Product;
import com.smartbrush.smartbrush_backend.jwt.JwtProvider;
import com.smartbrush.smartbrush_backend.repository.ProductRepository;
import com.smartbrush.smartbrush_backend.service.ProductRecommendationServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
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

    private final JwtProvider jwtProvider;
    private final ProductRecommendationServiceImpl recommendationService;
    private final ProductRepository productRepository;

    @Operation(
            summary = "카테고리별 맞춤 추천",
            description = "category에 shampoo/conditioner/essence/tonic/treatment 중 하나를 넣고, size로 개수 제한합니다."
    )
    @GetMapping("/{category}")
    public List<Product> getPersonalizedByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request
    ) {
        String token = jwtProvider.resolveToken(request);
        if (token == null || !jwtProvider.validateToken(token)) {
            return List.of();
        }
        String email = jwtProvider.getEmail(token);
        return recommendationService.recommendByCategory(email, category, size);
    }

    @Operation(summary = "모든 제품 가져오기", description = "size 파라미터로 개수 제한 가능합니다.")
    @GetMapping
    public List<Product> getAllProducts(@RequestParam(defaultValue = "20") int size) {
        int safeSize = Math.max(size, 1);
        Pageable pageable = PageRequest.of(0, safeSize);
        return productRepository.findAll(pageable).getContent();
    }
}

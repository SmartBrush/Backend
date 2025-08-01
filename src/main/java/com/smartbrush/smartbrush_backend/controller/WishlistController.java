package com.smartbrush.smartbrush_backend.controller;

import com.smartbrush.smartbrush_backend.entity.AuthEntity;
import com.smartbrush.smartbrush_backend.entity.Product;
import com.smartbrush.smartbrush_backend.repository.AuthRepository;
import com.smartbrush.smartbrush_backend.repository.ProductRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
@Tag(name = "추천제품 찜하기 API", description = "추천제품을 찜할 수 있는 기능입니다.")
public class WishlistController {

    private final AuthRepository authRepository;
    private final ProductRepository productRepository;

    @Operation(summary = "추천제품 찜하기", description = "추천제품을 찜합니다.")
    @PostMapping("/{productId}")
    public ResponseEntity<String> addToWishlist(HttpServletRequest request,
                                                @PathVariable Long productId) {
        Long userId = (Long) request.getAttribute("userId");
        AuthEntity user = authRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("상품이 존재하지 않습니다."));

        user.getWishlist().add(product);
        authRepository.save(user);
        return ResponseEntity.ok("찜 목록에 추가되었습니다.");
    }

    @Operation(summary = "찜한 제품 삭제", description = "찜한 제품을 삭제합니다.")
    @DeleteMapping("/{productId}")
    public ResponseEntity<String> removeFromWishlist(HttpServletRequest request,
                                                     @PathVariable Long productId) {
        Long userId = (Long) request.getAttribute("userId");
        AuthEntity user = authRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        user.getWishlist().removeIf(p -> p.getId().equals(productId));
        authRepository.save(user);
        return ResponseEntity.ok("찜 목록에서 삭제되었습니다.");
    }
    
    @Operation(summary = "찜한 제품 보기", description = "사용자가 찜한 제품을 불러옵니다.")
    @GetMapping
    public ResponseEntity<List<Product>> getWishlist(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        AuthEntity user = authRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        return ResponseEntity.ok(user.getWishlist());
    }
}

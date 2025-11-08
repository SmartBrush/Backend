package com.smartbrush.smartbrush_backend.controller;

import com.smartbrush.smartbrush_backend.dto.MBTI.ScalpMbtiSummaryDTO;
import com.smartbrush.smartbrush_backend.jwt.JwtProvider;
import com.smartbrush.smartbrush_backend.service.ScalpProfileServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/me")
@Tag(name = "사용자 MBTI", description = "제품 추천 페이지 MBTI API")
public class MeController {

    private final JwtProvider jwtProvider;
    private final ScalpProfileServiceImpl scalpProfileService;

    @GetMapping("/scalp-mbti")
    @Operation(summary = "내 두피 MBTI 요약", description = "닉네임, 두피 MBTI, 마지막 진단일 반환")
    public ResponseEntity<?> getMyScalpMbti(HttpServletRequest request) {
        String token = jwtProvider.resolveToken(request);
        if (token == null || !jwtProvider.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 토큰입니다.");
        }
        String email = jwtProvider.getEmail(token);

        ScalpMbtiSummaryDTO dto = scalpProfileService.getSummary(email);
        return ResponseEntity.ok(dto);
    }
}

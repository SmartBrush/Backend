package com.smartbrush.smartbrush_backend.controller;

import com.smartbrush.smartbrush_backend.dto.uv.UvRequestDto;
//import com.smartbrush.smartbrush_backend.dto.uv.UvResult;
import com.smartbrush.smartbrush_backend.entity.AuthEntity;
import com.smartbrush.smartbrush_backend.repository.AuthRepository;
import com.smartbrush.smartbrush_backend.repository.UvResultRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.smartbrush.smartbrush_backend.entity.UvResult;


import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/uv")
public class UvController {

    private final AuthRepository authRepository;
    private final UvResultRepository uvResultRepository;

    // UV 데이터 수신 → 사용자 정보와 함께 저장
    @PostMapping
    public ResponseEntity<String> receiveUV(@RequestBody UvRequestDto dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return ResponseEntity.status(401).body("Unauthorized");

        AuthEntity user = authRepository.findById(userId).orElseThrow();

        UvResult saved = uvResultRepository.save(
                UvResult.create(dto.getUv(), dto.getState(), dto.getDeviceId(), user)
        );

        return ResponseEntity.ok("UV 데이터 저장 완료: " + saved.getId());
    }

    // 로그인한 사용자의 마지막 UV 정보 조회
    @GetMapping("/latest")
    public ResponseEntity<UvResult> getLatestUV(HttpServletRequest request) {
        Object userIdAttr = request.getAttribute("userId");

        if (userIdAttr == null) {
            return ResponseEntity.status(401).build();  // 인증 정보 없음
        }

        Long userId;
        try {
            userId = Long.parseLong(userIdAttr.toString());
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();  // 잘못된 userId
        }

        return authRepository.findById(userId)
                .flatMap(user -> uvResultRepository.findTopByUserOrderByTimestampDesc(user))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }
}
package com.smartbrush.smartbrush_backend.controller;

import com.smartbrush.smartbrush_backend.dto.uv.UvRequestDto;
//import com.smartbrush.smartbrush_backend.dto.uv.UvResult;
import com.smartbrush.smartbrush_backend.dto.uv.UvResultResponseDto;
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
//@CrossOrigin(origins = "http://localhost:5173")
public class UvController {

    private final AuthRepository authRepository;
    private final UvResultRepository uvResultRepository;

    @PostMapping
    public ResponseEntity<String> receiveUV(@RequestBody UvRequestDto dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            System.out.println("❌ Unauthorized access attempt");
            return ResponseEntity.status(401).body("Unauthorized");
        }

        Integer uv = dto.getUv();
        if (uv == null || uv == 1 || uv == 2 || uv == 0) {
            System.out.println("⚠️ UV 값이 너무 낮아 저장되지 않음: " + uv);
            return ResponseEntity.ok("UV 값이 너무 낮아 저장되지 않음: " + uv);
        }

        AuthEntity user = authRepository.findById(userId).orElseThrow();

        UvResult saved = uvResultRepository.save(
                UvResult.create(uv, dto.getState(), dto.getDeviceId(), user)
        );

        List<UvResult> allByUser = uvResultRepository.findAllByUserOrderByTimestampDesc(user);
        if (allByUser.size() > 5) {
            List<UvResult> toDelete = allByUser.subList(5, allByUser.size());
            uvResultRepository.deleteAll(toDelete);
        }

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

    @GetMapping("/date")
    public ResponseEntity<UvResult> getUvByDate(
            @RequestParam("date") String dateString,
            HttpServletRequest request) {

        Object userIdAttr = request.getAttribute("userId");
        if (userIdAttr == null) {
            return ResponseEntity.status(401).build();
        }

        Long userId;
        try {
            userId = Long.parseLong(userIdAttr.toString());
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }

        // 사용자 조회
        return authRepository.findById(userId)
                .flatMap(user -> uvResultRepository.findByUserAndDate(user, dateString))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }


    @GetMapping("/all")
    public ResponseEntity<List<UvResultResponseDto>> getAllUserUv(HttpServletRequest request) {
        Object userIdAttr = request.getAttribute("userId");
        if (userIdAttr == null) {
            return ResponseEntity.status(401).build(); // 인증 정보 없음
        }

        Long userId;
        try {
            userId = Long.parseLong(userIdAttr.toString());
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }

        return authRepository.findById(userId)
                .map(user -> {
                    List<UvResultResponseDto> result = uvResultRepository
                            .findAllByUserOrderByTimestampDesc(user)
                            .stream()
                            .map(uv -> UvResultResponseDto.builder()
                                    .id(uv.getId())
                                    .uv(uv.getUv())
                                    .state(uv.getState())
                                    .deviceId(uv.getDeviceId())
                                    .timestamp(uv.getTimestamp())
                                    .userId(uv.getUser().getId()) // 여기서 사용자 ID 포함
                                    .build())
                            .toList();
                    return ResponseEntity.ok(result);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
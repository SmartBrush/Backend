package com.smartbrush.smartbrush_backend.controller;

import com.smartbrush.smartbrush_backend.dto.habit.DailyHabitResponseDTO;
import com.smartbrush.smartbrush_backend.repository.AuthRepository;
import com.smartbrush.smartbrush_backend.service.DailyHabitService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;

@RestController
@RequestMapping("/api/habits")
@RequiredArgsConstructor
public class DailyHabitController {

    private final DailyHabitService dailyHabitService;
    private final AuthRepository authRepository;

    @GetMapping("/daily")
    public ResponseEntity<DailyHabitResponseDTO> getDaily(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            Authentication authentication
    ) {
        LocalDate target = (date != null) ? date : LocalDate.now(ZoneId.of("Asia/Seoul"));

        Long userId = authRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"))
                .getId();

        DailyHabitResponseDTO dto = dailyHabitService.getOrCreateTodayChecklist(userId, target);
        return ResponseEntity.ok(dto);
    }

    /** 완료 상태 토글 (본문 없음) */
    @PatchMapping("/{habitId}")
    public ResponseEntity<Void> toggleCompletion(
            @PathVariable Long habitId,
            Authentication authentication
    ) {
        Long userId = authRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"))
                .getId();

        dailyHabitService.toggleCompletion(habitId, userId);
        return ResponseEntity.noContent().build();
    }
}
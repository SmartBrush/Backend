package com.smartbrush.smartbrush_backend.controller;

import com.smartbrush.smartbrush_backend.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Tag(name = "연속 출석일", description = "연속 출석일 API 입니다.")
@RequestMapping("/api/attendance")
public class AttendanceController {
    private final AttendanceService attendanceService;

    @Operation(summary = "연속 출석일", description = "연속 출석일 API 입니다.")
    @GetMapping
    public Map<String, Object> getMyAttendance(Authentication authentication) {
        String email = authentication.getName();
        int streak = attendanceService.getCurrentStreak(email);

        return Map.of(
                "email", email,
                "currentStreak", streak
        );
    }
}

package com.smartbrush.smartbrush_backend.dto.attendance;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AttendanceDTO {
    private String email;
    private int currentStreak;    // 연속 출석일
}

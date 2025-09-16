package com.smartbrush.smartbrush_backend.service;

public interface AttendanceService {
    // 연속 출석일수
    int getCurrentStreak(String email);
}

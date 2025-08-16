package com.smartbrush.smartbrush_backend.service;

import com.smartbrush.smartbrush_backend.dto.habit.DailyHabitResponseDTO;

import java.time.LocalDate;

public interface DailyHabitService {

    /**
     * 해당 사용자/날짜의 데일리 체크리스트를 조회하며,
     * 없으면 카테고리별 2개씩 생성 후 반환합니다.
     */
    DailyHabitResponseDTO getOrCreateTodayChecklist(Long userId, LocalDate date);

    /**
     * 체크리스트 항목의 완료 상태를 업데이트합니다.
     */
    void toggleCompletion(Long habitId, Long userId);
}

package com.smartbrush.smartbrush_backend.repository;

import com.smartbrush.smartbrush_backend.entity.AuthEntity;
import com.smartbrush.smartbrush_backend.entity.DailyHabitEntity;
import com.smartbrush.smartbrush_backend.entity.HabitCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DailyHabitRepository extends JpaRepository<DailyHabitEntity, Long> {

    List<DailyHabitEntity> findByUserAndHabitDate(AuthEntity user, LocalDate habitDate);

    boolean existsByUserAndHabitDateAndCategory(AuthEntity user, LocalDate date, HabitCategory category);

    boolean existsByUserAndHabitDateAndCategoryAndItemText(
            AuthEntity user, LocalDate date, HabitCategory category, String itemText
    );
}
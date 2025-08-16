package com.smartbrush.smartbrush_backend.service;

import com.smartbrush.smartbrush_backend.dto.habit.DailyHabitItemDTO;
import com.smartbrush.smartbrush_backend.dto.habit.DailyHabitResponseDTO;
import com.smartbrush.smartbrush_backend.entity.AuthEntity;
import com.smartbrush.smartbrush_backend.entity.DailyHabitEntity;
import com.smartbrush.smartbrush_backend.entity.HabitCategory;
import com.smartbrush.smartbrush_backend.repository.AuthRepository;
import com.smartbrush.smartbrush_backend.repository.DailyHabitRepository;
import com.smartbrush.smartbrush_backend.support.HabitCatalog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DailyHabitServiceImpl implements DailyHabitService {

    private final DailyHabitRepository dailyHabitRepository;
    private final AuthRepository authRepository;

    @Override
    @Transactional
    public DailyHabitResponseDTO getOrCreateTodayChecklist(Long userId, LocalDate date) {
        AuthEntity user = authRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<DailyHabitEntity> existing = dailyHabitRepository.findByUserAndHabitDate(user, date);
        if (existing.size() == 6) {
            return asResponse(date, existing);
        }

        List<DailyHabitEntity> toSave = new ArrayList<>();
        for (HabitCategory cat : HabitCategory.values()) {
            if (!dailyHabitRepository.existsByUserAndHabitDateAndCategory(user, date, cat)) {
                List<String> pool = HabitCatalog.CATALOG.get(cat);
                List<String> selected = pickTwoDeterministic(pool, user.getId(), date, cat);
                for (String s : selected) {
                    toSave.add(DailyHabitEntity.builder()
                            .user(user)
                            .habitDate(date)
                            .category(cat)
                            .itemText(s)
                            .completed(false)
                            .build());
                }
            }
        }

        if (!toSave.isEmpty()) {
            dailyHabitRepository.saveAll(toSave);
        }

        List<DailyHabitEntity> all = dailyHabitRepository.findByUserAndHabitDate(user, date);
        return asResponse(date, all);
    }

    @Override
    @Transactional
    public void toggleCompletion(Long habitId, Long userId) {
        DailyHabitEntity habit = dailyHabitRepository.findById(habitId)
                .orElseThrow(() -> new IllegalArgumentException("Habit not found"));

        if (!habit.getUser().getId().equals(userId)) {
            throw new IllegalStateException("No permission");
        }
        habit.setCompleted(!habit.isCompleted());
    }

    // -------- private helpers --------

    private List<String> pickTwoDeterministic(List<String> pool, Long userId, LocalDate date, HabitCategory cat) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String seedStr = userId + "|" + date + "|" + cat.name();
            byte[] hash = md.digest(seedStr.getBytes(StandardCharsets.UTF_8));
            long seed = toLong(hash);

            Random rnd = new Random(seed);
            List<String> copy = new ArrayList<>(pool);
            Collections.shuffle(copy, rnd);
            return copy.subList(0, Math.min(2, copy.size()));
        } catch (Exception e) {
            List<String> copy = new ArrayList<>(pool);
            Collections.shuffle(copy);
            return copy.subList(0, Math.min(2, copy.size()));
        }
    }

    private long toLong(byte[] bytes) {
        long res = 0;
        for (int i = 0; i < Math.min(8, bytes.length); i++) {
            res = (res << 8) | (bytes[i] & 0xff);
        }
        return res;
    }

    private DailyHabitResponseDTO asResponse(LocalDate date, List<DailyHabitEntity> habits) {
        Map<HabitCategory, List<DailyHabitItemDTO>> grouped = habits.stream()
                .sorted(Comparator.comparing(h -> h.getCategory().name()))
                .map(h -> DailyHabitItemDTO.builder()
                        .id(h.getId())
                        .category(h.getCategory())
                        .itemText(h.getItemText())
                        .completed(h.isCompleted())
                        .build())
                .collect(Collectors.groupingBy(
                        DailyHabitItemDTO::getCategory,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        return DailyHabitResponseDTO.builder()
                .date(date)
                .itemsByCategory(grouped)
                .build();
    }
}

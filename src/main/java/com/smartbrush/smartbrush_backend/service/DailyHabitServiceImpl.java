package com.smartbrush.smartbrush_backend.service;

import com.smartbrush.smartbrush_backend.dto.habit.DailyHabitItemDTO;
import com.smartbrush.smartbrush_backend.dto.habit.DailyHabitResponseDTO;
import com.smartbrush.smartbrush_backend.dto.question.UserProfileInfoResponseDTO;
import com.smartbrush.smartbrush_backend.entity.AuthEntity;
import com.smartbrush.smartbrush_backend.entity.DailyHabitEntity;
import com.smartbrush.smartbrush_backend.entity.HabitCategory;
import com.smartbrush.smartbrush_backend.repository.AuthRepository;
import com.smartbrush.smartbrush_backend.service.ProfileInfoService;
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
    private final ProfileInfoService profileInfoService;

    @Override
    @Transactional
    public DailyHabitResponseDTO getOrCreateTodayChecklist(Long userId, LocalDate date) {
        AuthEntity user = authRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<DailyHabitEntity> existing = dailyHabitRepository.findByUserAndHabitDate(user, date);
        if (existing.size() >= 6) {
            return asResponse(date, existing);
        }

        var profile = profileInfoService.getProfileInfo(user);



        List<DailyHabitEntity> toSave = new ArrayList<>();
        for (HabitCategory cat : HabitCategory.values()) {
            if (!dailyHabitRepository.existsByUserAndHabitDateAndCategory(user, date, cat)) {
                List<String> userPool = buildUserPool(cat, profile);

                List<String> merged = mergeWithFallback(cat, userPool);

                List<String> selected = pickTwoDeterministic(merged, user.getId(), date, cat);

                for (String s : selected) {
                    if (!dailyHabitRepository.existsByUserAndHabitDateAndCategoryAndItemText(user, date, cat, s)) {
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
        }


        if (!toSave.isEmpty()) {
            dailyHabitRepository.saveAll(toSave);
        }

        List<DailyHabitEntity> all = dailyHabitRepository.findByUserAndHabitDate(user, date);
        return asResponse(date, all);
    }

    // -------- 사용자 맞춤 풀 --------
    private List<String> buildUserPool(HabitCategory cat, UserProfileInfoResponseDTO p) {
        List<String> bag = new ArrayList<>();
        if (p == null || !p.isHasProfile()) {
            return bag; // 설문 없으면 빈 bag 반환
        }

        switch (cat) {
            case LIFESTYLE -> {
                if ("UNDER_4_HOURS".equals(s(p.getSleepDuration()))) {
                    addWeighted(bag, "밤 11시 이전에 수면", 2);
                    addWeighted(bag, "6-8시간 수면 유지", 3);
                    addWeighted(bag, "카페인 오후 2시 이후 제한", 2);
                }
                if (Boolean.TRUE.equals(p.getWearHatFrequently())) {
                    addWeighted(bag, "모자 착용 시간 줄이기", 3);
                    addWeighted(bag, "모자 안감 주 2회 세탁", 2);
                }
                if ("FREQUENT_OUTDOOR".equals(s(p.getUvExposureLevel()))) {
                    addWeighted(bag, "두피 자외선 차단", 3);
                    addWeighted(bag, "샤워 전 5분 환기하기", 1);
                }
                addWeighted(bag, "스트레스 관리하기", 2);
                addWeighted(bag, "출퇴근 10분 걷기", 2);
                addWeighted(bag, "사무실 가습기 가동", 1);
            }
            case SCALP -> {
                if (Boolean.TRUE.equals(p.getDyedOrPermedRecently())) {
                    addWeighted(bag, "트리트먼트는 모발에만", 2);
                    addWeighted(bag, "저자극/무실리콘 샴푸 테스트", 2);
                    addWeighted(bag, "드라이 전 열보호제 사용", 3);
                }
                if (contains(p.getScalpSymptoms(), "DRY_SCALP")) {
                    addWeighted(bag, "두피 건조 후 수분 토닉 사용", 3);
                    addWeighted(bag, "두피 마스크 or 팩하기", 2);
                }
                if (contains(p.getScalpSymptoms(), "OILY_SCALP")) {
                    addWeighted(bag, "샴푸 3분 이상 헹구기", 3);
                    addWeighted(bag, "미지근한 물로 샴푸", 2);
                }
                if ("FREQUENT_OUTDOOR".equals(s(p.getUvExposureLevel()))) {
                    addWeighted(bag, "두피 자외선 스프레이 사용", 3);
                    addWeighted(bag, "두피 쿨링 겔 활용", 1);
                }
                addWeighted(bag, "머리 감은 후 즉시 건조", 2);
                addWeighted(bag, "정기적으로 빗 세척 및 교체", 1);
            }
            case NUTRITION -> {
                if (contains(p.getEatingHabits(), "IRREGULAR_MEALS")) {
                    addWeighted(bag, "아침 식사 거르지 않기", 3);
                    addWeighted(bag, "하루 3끼 균형잡힌 식사", 2);
                }
                if (contains(p.getEatingHabits(), "PREFER_HIGH_FAT")) {
                    addWeighted(bag, "고지방/튀김류 줄이기", 3);
                    addWeighted(bag, "가공 식품 줄이기", 2);
                }
                addWeighted(bag, "하루 1회 단백질- 계란,생선 섭취", 2);
                addWeighted(bag, "아연-호박씨,굴,조개류 섭취", 1);
                addWeighted(bag, "유산균 섭취로 장건강 유지", 2);
                addWeighted(bag, "견과류 한 줌 간식", 1);
            }
        }
        return bag;
    }

    private List<String> mergeWithFallback(HabitCategory cat, List<String> userPool) {
        // 가중 bag → 유니크화
        LinkedHashSet<String> uniq = new LinkedHashSet<>(userPool);
        var base = HabitCatalog.CATALOG.getOrDefault(cat, List.of());
        for (String s : base) {
            if (uniq.size() >= 6) break;
            uniq.add(s);
        }
        // 최종 후보
        return new ArrayList<>(uniq);
    }

    private void addWeighted(List<String> bag, String item, int weight) {
        for (int i = 0; i < weight; i++) bag.add(item);
    }

    private boolean contains(Set<String> set, String v) {
        return set != null && set.contains(v);
    }

    private String s(Enum<?> e) {
        return e == null ? null : e.name();
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

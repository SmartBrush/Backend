package com.smartbrush.smartbrush_backend.dto.habit;


import com.smartbrush.smartbrush_backend.entity.HabitCategory;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyHabitResponseDTO {
    private LocalDate date;
    private Map<HabitCategory, List<DailyHabitItemDTO>> itemsByCategory;
}

package com.smartbrush.smartbrush_backend.dto.habit;

import com.smartbrush.smartbrush_backend.entity.HabitCategory;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyHabitItemDTO {
    private Long id;
    private HabitCategory category;
    private String itemText;
    private boolean completed;
}

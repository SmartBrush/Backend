package com.smartbrush.smartbrush_backend.dto.question;

import com.smartbrush.smartbrush_backend.entity.QuestionEnum.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;


@Getter
@Setter
@NoArgsConstructor
public class ProfileInfoRequestDTO {
    private Gender gender;
    private int age;
    private HairLength hairLength;

    private Boolean dyedOrPermedRecently;
    private FamilyHairLoss familyHairLoss;

    private Boolean wearHatFrequently;
    private UvExposureLevel uvExposureLevel;

    private WashingFrequency washingFrequency;

    private Set<HairProductType> usingProducts;

    private Set<EatingHabit> eatingHabits;

    private Set<ScalpSymptom> scalpSymptoms;

    private SleepDuration sleepDuration;
    private SleepStartTime sleepStartTime;
}
package com.smartbrush.smartbrush_backend.dto.question;

import com.smartbrush.smartbrush_backend.entity.QuestionEnum.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Set;

@Getter
@AllArgsConstructor
public class UserProfileInfoResponseDTO {
    private Long id;
    private String nickname;
    private String email;

    private Gender gender;
    private int age;
    private HairLength hairLength;

    private Boolean dyedOrPermedRecently;
    private FamilyHairLoss familyHairLoss;

    private Boolean wearHatFrequently;
    private UvExposureLevel uvExposureLevel;

    private WashingFrequency washingFrequency;

//    private Set<HairProductType> usingProducts;
    private List<String> usingProducts;

//    private Set<EatingHabit> eatingHabits;
//
//    private Set<ScalpSymptom> scalpSymptoms;
    private Set<String> eatingHabits;
    private Set<String> scalpSymptoms;

    private SleepDuration sleepDuration;
    private SleepStartTime sleepStartTime;

}
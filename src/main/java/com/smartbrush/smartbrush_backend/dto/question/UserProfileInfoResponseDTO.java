package com.smartbrush.smartbrush_backend.dto.question;

import com.smartbrush.smartbrush_backend.entity.QuestionEnum.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Set;

@Getter
@AllArgsConstructor
public class UserProfileInfoResponseDTO {
    private boolean hasProfile;   // 프로필 존재 여부

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
    private List<String> usingProducts;
    private Set<String> eatingHabits;
    private Set<String> scalpSymptoms;

    private SleepDuration sleepDuration;
    private SleepStartTime sleepStartTime;

    // 프로필 없는 경우
    public static UserProfileInfoResponseDTO empty(String nickname, String email) {
        return new UserProfileInfoResponseDTO(
                false,          // hasProfile
                null,           // id
                nickname,
                email,
                null,           // gender
                0,              // age (primitive 이라 0)
                null,           // hairLength
                null,           // dyedOrPermedRecently
                null,           // familyHairLoss
                null,           // wearHatFrequently
                null,           // uvExposureLevel
                null,           // washingFrequency
                List.of(),      // usingProducts
                Set.of(),       // eatingHabits
                Set.of(),       // scalpSymptoms
                null,           // sleepDuration
                null            // sleepStartTime
        );
    }
}
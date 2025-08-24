//package com.smartbrush.smartbrush_backend.entity;
//
//import com.smartbrush.smartbrush_backend.entity.QuestionEnum.*;
//import jakarta.persistence.*;
//import lombok.*;
//
//import java.util.HashSet;
//import java.util.Set;
//
//@Entity
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class UserProfileInfo {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Enumerated(EnumType.STRING)
//    private Gender gender;
//
//    private int age;
//
//    @Enumerated(EnumType.STRING)
//    private HairLength hairLength;
//
//    @Column
//    private Boolean dyedOrPermedRecently;
//
//    @Enumerated(EnumType.STRING)
//    private FamilyHairLoss familyHairLoss;
//
//    @Column
//    private Boolean wearHatFrequently;
//
//    @Enumerated(EnumType.STRING)
//    private UvExposureLevel uvExposureLevel;
//
//    @Enumerated(EnumType.STRING)
//    private WashingFrequency washingFrequency;
//
//    @ElementCollection(fetch = FetchType.LAZY)
//    @CollectionTable(name = "user_hair_products", joinColumns = @JoinColumn(name = "user_id"))
//    @Enumerated(EnumType.STRING)
//    private Set<HairProductType> usingProducts = new HashSet<>();
//
//    @ElementCollection(fetch = FetchType.LAZY)
//    @CollectionTable(name = "user_eating_habits", joinColumns = @JoinColumn(name = "user_id"))
//    @Enumerated(EnumType.STRING)
//    private Set<EatingHabit> eatingHabits = new HashSet<>();
//
//    @ElementCollection(fetch = FetchType.LAZY)
//    @CollectionTable(name = "user_scalp_symptoms", joinColumns = @JoinColumn(name = "user_id"))
//    @Enumerated(EnumType.STRING)
//    private Set<ScalpSymptom> scalpSymptoms = new HashSet<>();
//
//    @Enumerated(EnumType.STRING)
//    private SleepDuration sleepDuration;
//
//    @Enumerated(EnumType.STRING)
//    private SleepStartTime sleepStartTime;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "auth_entity_id") // FK 이름
//    private AuthEntity authEntity;
//}


package com.smartbrush.smartbrush_backend.entity;

import com.smartbrush.smartbrush_backend.entity.QuestionEnum.*;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private int age;

    @Enumerated(EnumType.STRING)
    private HairLength hairLength;

    @Column
    private Boolean dyedOrPermedRecently;

    @Enumerated(EnumType.STRING)
    private FamilyHairLoss familyHairLoss;

    @Column
    private Boolean wearHatFrequently;

    @Enumerated(EnumType.STRING)
    private UvExposureLevel uvExposureLevel;

    @Enumerated(EnumType.STRING)
    private WashingFrequency washingFrequency;

    @Builder.Default
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "user_hair_products",
            joinColumns = @JoinColumn(name = "user_profile_info_id") // ← 요렇게!
    )
    @Column(name = "product_type", length = 64)
    @Enumerated(EnumType.STRING)
    private Set<HairProductType> usingProducts = new HashSet<>();

    // Eating habits
    @Builder.Default
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "user_eating_habits",
            joinColumns = @JoinColumn(name = "user_profile_info_id")
    )
    @Column(name = "eating_habit", length = 64)
    @Enumerated(EnumType.STRING)
    private Set<EatingHabit> eatingHabits = new HashSet<>();

    // Scalp symptoms
    @Builder.Default
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "user_scalp_symptoms",
            joinColumns = @JoinColumn(name = "user_profile_info_id")
    )
    @Column(name = "scalp_symptom", length = 64)
    @Enumerated(EnumType.STRING)
    private Set<ScalpSymptom> scalpSymptoms = new HashSet<>();

    @Enumerated(EnumType.STRING)
    private SleepDuration sleepDuration;

    @Enumerated(EnumType.STRING)
    private SleepStartTime sleepStartTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auth_entity_id")
    private AuthEntity authEntity;
}

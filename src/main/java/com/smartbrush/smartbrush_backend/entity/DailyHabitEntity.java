package com.smartbrush.smartbrush_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(indexes = {
        @Index(name = "idx_dailyhabit_user_date", columnList = "user_id,habitDate")
},uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_user_date_cat_text",
                columnNames = {"user_id","habitDate","category","itemText"}
        )
})
public class DailyHabitEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 로그인 사용자
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private AuthEntity user;

    // 어떤 날짜의 체크리스트인지
    @Column(nullable = false)
    private LocalDate habitDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private HabitCategory category;

    // 선택된 아이템(문구 자체 저장: 나중에 원본 리스트 변경돼도 일관성 유지)
    @Column(nullable = false, length = 200)
    private String itemText;

    // 완료 여부
    @Column(nullable = false)
    private boolean completed;
}

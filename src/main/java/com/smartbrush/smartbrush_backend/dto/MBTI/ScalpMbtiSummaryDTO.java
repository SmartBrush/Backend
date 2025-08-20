package com.smartbrush.smartbrush_backend.dto.MBTI;

import lombok.*;
import java.time.LocalDate;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ScalpMbtiSummaryDTO {
    private String nickname;           // 사용자 이름
    private String email;          // 식별용
    private String scalpMbti;      // 두피 MBTI
    private LocalDate diagnosedDate; // 마지막 진단일
}

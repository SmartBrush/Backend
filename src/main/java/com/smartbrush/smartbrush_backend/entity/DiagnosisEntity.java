package com.smartbrush.smartbrush_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor
public class DiagnosisEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private LocalDate diagnosedDate;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String resultJson;

    public DiagnosisEntity(String email, LocalDate diagnosedDate, String resultJson) {
        this.email = email;
        this.diagnosedDate = diagnosedDate;
        this.resultJson = resultJson;
    }

    // 진단 결과 갱신 시 사용
    public void updateResult(String resultJson) {
        this.resultJson = resultJson;
        this.diagnosedDate = LocalDate.now();
    }
}

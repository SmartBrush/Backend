package com.smartbrush.smartbrush_backend.repository;

import com.smartbrush.smartbrush_backend.entity.DiagnosisEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DiagnosisRepository extends JpaRepository<DiagnosisEntity, Long> {
    Optional<DiagnosisEntity> findByEmailAndDiagnosedDate(String email, LocalDate date);
    List<DiagnosisEntity> findAllByEmail(String email); // 전체 기록 조회용
    Optional<DiagnosisEntity> findTopByEmailOrderByDiagnosedDateDesc(String email);
}
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

    // 월별 조회
    List<DiagnosisEntity> findAllByEmailAndDiagnosedDateBetween(String email, LocalDate start, LocalDate end);

    // 날짜별로 모든 진단 결과 조회
    List<DiagnosisEntity> findAllByEmailAndDiagnosedDate(String email, LocalDate diagnosedDate);

    // 사용자별 날짜 최신순 조회 - 출석일수 계산
    List<DiagnosisEntity> findByEmailOrderByDiagnosedDateDesc(String email);

    // 누적 출석일수
    long countByEmail(String email);
}
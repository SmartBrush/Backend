package com.smartbrush.smartbrush_backend.repository;

import com.smartbrush.smartbrush_backend.entity.DiagnosisImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.time.LocalDateTime;

public interface DiagnosisImageRepository extends JpaRepository<DiagnosisImageEntity, Long> {
    List<DiagnosisImageEntity> findTop100ByEmailOrderByCapturedAtDesc(String email);
    List<DiagnosisImageEntity> findTop4ByEmailOrderByCapturedAtDesc(String email);
}

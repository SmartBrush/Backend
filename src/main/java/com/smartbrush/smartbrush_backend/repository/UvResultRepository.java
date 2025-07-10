package com.smartbrush.smartbrush_backend.repository;


import com.smartbrush.smartbrush_backend.entity.AuthEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import com.smartbrush.smartbrush_backend.entity.UvResult;


import java.util.Optional;
public interface UvResultRepository extends JpaRepository<UvResult, Long> {
    Optional<UvResult> findTopByUserOrderByTimestampDesc(AuthEntity user);
}


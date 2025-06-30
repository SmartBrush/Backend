package com.smartbrush.smartbrush_backend.repository;


import com.smartbrush.smartbrush_backend.entity.AuthEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthRepository extends JpaRepository<AuthEntity, Long> {
    boolean existsByEmail(String email);
    Optional<AuthEntity> findByEmail(String email);
}
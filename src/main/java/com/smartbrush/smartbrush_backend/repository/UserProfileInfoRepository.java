package com.smartbrush.smartbrush_backend.repository;

import com.smartbrush.smartbrush_backend.entity.AuthEntity;
import com.smartbrush.smartbrush_backend.entity.UserProfileInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserProfileInfoRepository extends JpaRepository<UserProfileInfo, Long> {
    Optional<UserProfileInfo> findByAuthEntity(AuthEntity authEntity);
    void deleteByAuthEntity(AuthEntity authEntity);
}

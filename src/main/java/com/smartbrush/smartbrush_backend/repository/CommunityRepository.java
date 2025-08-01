package com.smartbrush.smartbrush_backend.repository;

import com.smartbrush.smartbrush_backend.entity.Community;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface CommunityRepository extends JpaRepository<Community, Long> {
    List<Community> findByUserId(Long userId);
}

package com.smartbrush.smartbrush_backend.repository;

import com.smartbrush.smartbrush_backend.entity.Community;
import org.springframework.data.jpa.repository.JpaRepository;


public interface CommunityRepository extends JpaRepository<Community, Long> {
}

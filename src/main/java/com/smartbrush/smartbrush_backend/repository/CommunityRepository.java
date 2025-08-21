package com.smartbrush.smartbrush_backend.repository;

import com.smartbrush.smartbrush_backend.entity.Community;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface CommunityRepository extends JpaRepository<Community, Long> {
    List<Community> findByUserId(Long userId);
    
    // 고민공유 검색
    List<Community> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCaseOrderByIdDesc(
            String titleKeyword, String contentKeyword
    );
}

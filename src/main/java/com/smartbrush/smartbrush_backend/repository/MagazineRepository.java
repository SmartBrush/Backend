package com.smartbrush.smartbrush_backend.repository;

import com.smartbrush.smartbrush_backend.entity.Magazine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MagazineRepository extends JpaRepository<Magazine, Long> {
    // 칼럼 전체 리스트
    Page<Magazine> findAll(Pageable pageable);
    
    // 칼럼 10개 랜덤
    @Query(value = "SELECT * FROM magazine ORDER BY RAND() LIMIT 10", nativeQuery = true)
    List<Magazine> getRecommend();
    
    // 칼럼 검색
    Page<Magazine> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);
}

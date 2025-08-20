package com.smartbrush.smartbrush_backend.repository;

import com.smartbrush.smartbrush_backend.controller.CommunityController;
import com.smartbrush.smartbrush_backend.entity.CommunityLikeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommunityLikeRepository extends JpaRepository<CommunityLikeEntity, Long> {

    long countByCommunity_Id(Long communityId);

    boolean existsByCommunity_IdAndMember_Id(Long communityId, Long memberId);

    void deleteByCommunity_IdAndMember_Id(Long communityId, Long memberId);


    List<CommunityLikeEntity> findByMember_Id(Long memberId);
}
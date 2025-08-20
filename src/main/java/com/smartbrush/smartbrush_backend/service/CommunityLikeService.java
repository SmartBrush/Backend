package com.smartbrush.smartbrush_backend.service;

import com.smartbrush.smartbrush_backend.entity.Community;
import com.smartbrush.smartbrush_backend.entity.CommunityLikeEntity;
import com.smartbrush.smartbrush_backend.entity.AuthEntity;
import com.smartbrush.smartbrush_backend.repository.CommunityLikeRepository;
import com.smartbrush.smartbrush_backend.repository.CommunityRepository;
import com.smartbrush.smartbrush_backend.repository.AuthRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommunityLikeService {

    private final CommunityRepository communityRepository;
    private final AuthRepository memberRepository;
    private final CommunityLikeRepository communityLikeRepository;

    @Transactional
    public void like(Long communityId, Long memberId) {
        if (communityLikeRepository.existsByCommunity_IdAndMember_Id(communityId, memberId)) return;

        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));
        AuthEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

        CommunityLikeEntity like = CommunityLikeEntity.builder()
                .community(community)
                .member(member)
                .build();

        communityLikeRepository.save(like);
    }

    @Transactional
    public void unlike(Long communityId, Long memberId) {
        communityLikeRepository.deleteByCommunity_IdAndMember_Id(communityId, memberId);
    }

    @Transactional(readOnly = true)
    public long getLikeCount(Long communityId) {
        return communityLikeRepository.countByCommunity_Id(communityId);
    }

    @Transactional(readOnly = true)
    public boolean isLiked(Long communityId, Long memberId) {
        return communityLikeRepository.existsByCommunity_IdAndMember_Id(communityId, memberId);
    }



    // 🔽 추가: 내가 좋아요한 전체
    @Transactional(readOnly = true)
    public List<CommunityLikeEntity> getMyLiked(Long memberId) {
        return communityLikeRepository.findByMember_Id(memberId);
    }
}

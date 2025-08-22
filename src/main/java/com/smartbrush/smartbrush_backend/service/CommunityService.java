package com.smartbrush.smartbrush_backend.service;

import com.smartbrush.smartbrush_backend.dto.community.CommunityRequestDTO;
import com.smartbrush.smartbrush_backend.dto.community.CommunityResponseDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface CommunityService {
    CommunityResponseDTO createCommunity(CommunityRequestDTO dto, Long userId, String author, String profileImage);
    CommunityResponseDTO getCommunity(Long id);
    List<CommunityResponseDTO> getAllCommunities();
    CommunityResponseDTO updateCommunity(Long id, CommunityRequestDTO dto, Long userId);
    void deleteCommunity(Long id, Long userId);
    CommunityResponseDTO getCommunity(Long id, Long userId);
    List<CommunityResponseDTO> getAllCommunities(Long userId);

    long like(Long communityId, Long userId);
    long unlike(Long communityId, Long userId);

    List<Long> getMyLikedPostIds(Long memberId);

    List<CommunityResponseDTO> searchCommunities(String keyword, Long userId);
}

package com.smartbrush.smartbrush_backend.service;

import com.smartbrush.smartbrush_backend.dto.community.CommunityRequestDTO;
import com.smartbrush.smartbrush_backend.dto.community.CommunityResponseDTO;

import java.util.List;

public interface CommunityService {
    CommunityResponseDTO createCommunity(CommunityRequestDTO dto, Long userId, String author, String profileImage);
    CommunityResponseDTO getCommunity(Long id);
    List<CommunityResponseDTO> getAllCommunities();
    CommunityResponseDTO updateCommunity(Long id, CommunityRequestDTO dto, Long userId);
    void deleteCommunity(Long id, Long userId);
}

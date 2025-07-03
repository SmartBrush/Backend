package com.smartbrush.smartbrush_backend.service;

import com.smartbrush.smartbrush_backend.entity.Community;
import com.smartbrush.smartbrush_backend.dto.community.CommunityRequestDTO;
import com.smartbrush.smartbrush_backend.dto.community.CommunityResponseDTO;
import com.smartbrush.smartbrush_backend.exception.CommunityException;
import com.smartbrush.smartbrush_backend.code.ErrorCode;
import com.smartbrush.smartbrush_backend.repository.CommunityRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommunityServiceImpl implements CommunityService {
    private final CommunityRepository communityRepository;

    @Override
    public CommunityResponseDTO createCommunity(CommunityRequestDTO dto, Long userId, String author, String profileImage) {
        Community community = new Community(dto.getTitle(), dto.getContent(), author, profileImage, userId);
        Community saved = communityRepository.save(community);
        return toDTO(saved);
    }

    @Override
    public CommunityResponseDTO getCommunity(Long id) {
        return communityRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new CommunityException(ErrorCode.POST_NOT_FOUND));
    }

    @Override
    public List<CommunityResponseDTO> getAllCommunities() {
        return communityRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CommunityResponseDTO updateCommunity(Long id, CommunityRequestDTO dto, Long userId) {
        Community community = getCommunityEntity(id);
        if (!community.getUserId().equals(userId)) {
            throw new CommunityException(ErrorCode.FORBIDDEN);
        }
        community.update(dto.getTitle(), dto.getContent());

        // DB에 반영
        communityRepository.save(community);

        return toDTO(community);
    }

    @Override
    public void deleteCommunity(Long id, Long userId) {
        Community community = getCommunityEntity(id);
        if (!community.getUserId().equals(userId)) {
            throw new CommunityException(ErrorCode.FORBIDDEN);
        }
        communityRepository.delete(community);
    }

    private Community getCommunityEntity(Long id) {
        return communityRepository.findById(id)
                .orElseThrow(() -> new CommunityException(ErrorCode.POST_NOT_FOUND));
    }

    private CommunityResponseDTO toDTO(Community community) {
        return CommunityResponseDTO.builder()
                .id(community.getId())
                .title(community.getTitle())
                .content(community.getContent())
                .author(community.getAuthor())
                .profileImage(community.getProfileImage())
                .createdAt(community.getCreatedAt())
                .build();
    }
}

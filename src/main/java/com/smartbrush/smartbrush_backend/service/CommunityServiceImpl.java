package com.smartbrush.smartbrush_backend.service;

import com.smartbrush.smartbrush_backend.entity.Community;
import com.smartbrush.smartbrush_backend.dto.community.CommunityRequestDTO;
import com.smartbrush.smartbrush_backend.dto.community.CommunityResponseDTO;
import com.smartbrush.smartbrush_backend.exception.CommunityException;
import com.smartbrush.smartbrush_backend.code.ErrorCode;
import com.smartbrush.smartbrush_backend.repository.CommentRepository;
import com.smartbrush.smartbrush_backend.repository.CommunityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommunityServiceImpl implements CommunityService {

    private final CommunityRepository communityRepository;
    private final CommunityLikeService communityLikeService;
    private final CommentRepository commentRepository;

    @Override
    public long like(Long communityId, Long userId) {
        communityLikeService.like(communityId, userId);
        return communityLikeService.getLikeCount(communityId);
    }

    @Override
    public long unlike(Long communityId, Long userId) {
        communityLikeService.unlike(communityId, userId);
        return communityLikeService.getLikeCount(communityId);
    }

    @Override
    public List<Long> getMyLikedPostIds(Long memberId) {
        return communityLikeService.getMyLiked(memberId)
                .stream()
                .map(l -> l.getCommunity().getId())
                .distinct() // 중복 방지(이론상 없겠지만 안전)
                .collect(Collectors.toList());
    }

    // 고민공유 글쓰기
    @Override
    public CommunityResponseDTO createCommunity(CommunityRequestDTO dto, Long userId, String author, String profileImage) {
        Community community = new Community(dto.getTitle(), dto.getContent(), author, profileImage, userId);
        Community saved = communityRepository.save(community);
        return toDTO(saved, 0L, false, 0L);
    }

    @Override
    public CommunityResponseDTO getCommunity(Long id) {
        return null;
    }

    @Override
    public List<CommunityResponseDTO> getAllCommunities() {
        return List.of();
    }

    // 고민공유 디테일 페이지
    @Override
    public CommunityResponseDTO getCommunity(Long id, Long userId) {
        Community community = communityRepository.findById(id)
                .orElseThrow(() -> new CommunityException(ErrorCode.POST_NOT_FOUND));

        long likeCount = communityLikeService.getLikeCount(id);
        boolean liked = communityLikeService.isLiked(id, userId);
        long commentCnt = commentRepository.countByCommunity_Id(id);

        return toDTO(community, likeCount, liked, commentCnt);
    }

    // 고민공유 전체 리스트 (로그인 유저 필수)
    @Override
    public List<CommunityResponseDTO> getAllCommunities(Long userId) {
        return communityRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(c -> {
                    long likeCount = communityLikeService.getLikeCount(c.getId());
                    boolean liked = communityLikeService.isLiked(c.getId(), userId);
                    long commentCnt = commentRepository.countByCommunity_Id(c.getId());
                    return toDTO(c, likeCount, liked, commentCnt);
                })
                .collect(Collectors.toList());
    }

    // 고민공유 글 수정하기
    @Override
    public CommunityResponseDTO updateCommunity(Long id, CommunityRequestDTO dto, Long userId) {
        Community community = getCommunityEntity(id);

        if (!community.getUserId().equals(userId)) {
            throw new CommunityException(ErrorCode.FORBIDDEN);
        }
        community.update(dto.getTitle(), dto.getContent());
        communityRepository.save(community);

        long likeCount = communityLikeService.getLikeCount(id);
        boolean liked = communityLikeService.isLiked(id, userId);
        long commentCnt = commentRepository.countByCommunity_Id(id);

        return toDTO(community, likeCount, liked, commentCnt);
    }

    // 고민공유 글 삭제하기
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

    private CommunityResponseDTO toDTO(Community community, long likeCount, boolean liked, long commentCount) {
        return CommunityResponseDTO.builder()
                .id(community.getId())
                .title(community.getTitle())
                .content(community.getContent())
                .author(community.getAuthor())
                .profileImage(community.getProfileImage())
                .createdAt(community.getCreatedAt())
                .likeCount(likeCount)
                .liked(liked)
                .commentCount(commentCount)
                .build();
    }

    @Override
    public List<CommunityResponseDTO> searchCommunities(String keyword, Long userId) {
        if (keyword == null || keyword.isBlank()) {
            return Collections.emptyList();
        }
        String kw = keyword.trim();

        List<Community> entities = communityRepository
                .findByTitleContainingIgnoreCaseOrContentContainingIgnoreCaseOrderByIdDesc(kw, kw);

        return entities.stream()
                .map(e -> this.getCommunity(e.getId(), userId))
                .toList();
    }
}

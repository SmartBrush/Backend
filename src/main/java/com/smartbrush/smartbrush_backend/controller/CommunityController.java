package com.smartbrush.smartbrush_backend.controller;

import com.smartbrush.smartbrush_backend.dto.community.CommunityRequestDTO;
import com.smartbrush.smartbrush_backend.dto.community.CommunityResponseDTO;
import com.smartbrush.smartbrush_backend.code.ResponseCode;
import com.smartbrush.smartbrush_backend.service.CommunityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
@Tag(name = "커뮤니티 고민공유", description = "커뮤니티 고민공유 CRUD/좋아요")
public class CommunityController {

    private final CommunityService communityService;
    private record LikeToggleResponse(boolean liked, long likeCount) {}


    @Operation(summary = "게시글 생성", description = "커뮤니티 고민공유를 생성합니다.")
    @PostMapping("/create")
    public ResponseEntity<CommunityResponseDTO> createCommunity(@RequestBody CommunityRequestDTO dto,
                                                                @RequestAttribute Long userId,
                                                                @RequestAttribute String author,
                                                                @RequestAttribute String profileImage) {
        CommunityResponseDTO response = communityService.createCommunity(dto, userId, author, profileImage);
        return ResponseEntity.status(ResponseCode.CREATED.getStatus()).body(response);
    }

    @Operation(summary = "전체 게시글 조회", description = "전체 고민공유를 조회합니다. (likeCount/liked 포함)")
    @GetMapping("/list")
    public ResponseEntity<List<CommunityResponseDTO>> getAllCommunities(
            @RequestAttribute(required = false) Long userId) {
        return ResponseEntity.status(ResponseCode.SUCCESS.getStatus())
                .body(communityService.getAllCommunities(userId));
    }

    @Operation(summary = "단일 게시글 조회", description = "고민공유를 조회하여 자세히 볼 수 있습니다. (likeCount/liked 포함)")
    @GetMapping("/detail/{id}")
    public ResponseEntity<CommunityResponseDTO> getCommunity(@PathVariable Long id,
                                                             @RequestAttribute(required = false) Long userId) {
        return ResponseEntity.status(ResponseCode.SUCCESS.getStatus())
                .body(communityService.getCommunity(id, userId));
    }

    @Operation(summary = "게시글 수정", description = "글을 쓴 사용자만 고민공유를 수정 가능합니다.")
    @PutMapping("/update/{id}")
    public ResponseEntity<CommunityResponseDTO> updateCommunity(@PathVariable Long id,
                                                                @RequestBody CommunityRequestDTO dto,
                                                                @RequestAttribute Long userId) {
        return ResponseEntity.status(ResponseCode.SUCCESS.getStatus()).body(communityService.updateCommunity(id, dto, userId));
    }

    @Operation(summary = "게시글 삭제", description = "글을 쓴 사용자만 고민공유를 삭제 가능합니다.")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteCommunity(@PathVariable Long id,
                                                @RequestAttribute Long userId) {
        communityService.deleteCommunity(id, userId);
        return ResponseEntity.status(ResponseCode.SUCCESS.getStatus()).build();
    }


    @Operation(summary = "게시글 좋아요")
    @PostMapping("/{id}/like")
    public ResponseEntity<LikeToggleResponse> like(@PathVariable Long id,
                                                   @RequestAttribute Long userId) {
        long likeCount = communityService.like(id, userId);
        return ResponseEntity.ok(new LikeToggleResponse(true, likeCount));
    }

    @Operation(summary = "게시글 좋아요 취소")
    @DeleteMapping("/{id}/like")
    public ResponseEntity<LikeToggleResponse> unlike(@PathVariable Long id,
                                                     @RequestAttribute Long userId) {
        long likeCount = communityService.unlike(id, userId);
        return ResponseEntity.ok(new LikeToggleResponse(false, likeCount));
    }

    @Operation(summary = "내가 좋아요한 글 목록(ID 배열)")
    @GetMapping("/likes")
    public ResponseEntity<List<Long>> myLikes(@RequestAttribute Long userId) {
        return ResponseEntity.ok(communityService.getMyLikedPostIds(userId));
    }

    @Operation(summary = "게시글 검색", description = "제목/본문 통합 검색")
    @GetMapping("/search")
    public ResponseEntity<List<CommunityResponseDTO>> searchCommunities(
            @RequestParam String keyword,
            @RequestAttribute(required = false) Long userId
    ) {
        var result = communityService.searchCommunities(keyword, userId);
        return ResponseEntity.status(ResponseCode.SUCCESS.getStatus()).body(result);
    }
}

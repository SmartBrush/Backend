package com.smartbrush.smartbrush_backend.controller;

import com.smartbrush.smartbrush_backend.dto.community.CommunityRequestDTO;
import com.smartbrush.smartbrush_backend.dto.community.CommunityResponseDTO;
import com.smartbrush.smartbrush_backend.code.ResponseCode;
import com.smartbrush.smartbrush_backend.service.CommunityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
@Tag(name = "커뮤니티 고민공유", description = "커뮤니티 고민공유 CRUD 입니다.")
public class CommunityController {

    private final CommunityService communityService;

    @Operation(summary = "게시글 생성", description = "커뮤니티 고민공유를 생성합니다.")
    @PostMapping("/create")
    public ResponseEntity<CommunityResponseDTO> createCommunity(@RequestBody CommunityRequestDTO dto,
                                                                @RequestAttribute Long userId,
                                                                @RequestAttribute String author,
                                                                @RequestAttribute String profileImage) {
        CommunityResponseDTO response = communityService.createCommunity(dto, userId, author, profileImage);
        return ResponseEntity.status(ResponseCode.CREATED.getStatus()).body(response);
    }

    @Operation(summary = "전체 게시글 조회", description = "전체 고민공유를 조회합니다.")
    @GetMapping("/list")
    public ResponseEntity<List<CommunityResponseDTO>> getAllCommunities() {
        return ResponseEntity.status(ResponseCode.SUCCESS.getStatus()).body(communityService.getAllCommunities());
    }

    @Operation(summary = "단일 게시글 조회", description = "고민공유를 조회하여 자세히 볼 수 있습니다.")
    @GetMapping("/detail/{id}")
    public ResponseEntity<CommunityResponseDTO> getCommunity(@PathVariable Long id) {
        return ResponseEntity.status(ResponseCode.SUCCESS.getStatus()).body(communityService.getCommunity(id));
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
}

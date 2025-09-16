package com.smartbrush.smartbrush_backend.controller;

import com.smartbrush.smartbrush_backend.dto.comment.CommentResponseDTO;
import com.smartbrush.smartbrush_backend.dto.community.CommunityResponseDTO;
import com.smartbrush.smartbrush_backend.dto.mypage.MyPageResponseDTO;
import com.smartbrush.smartbrush_backend.dto.profile.ProfileUpdateRequest;
import com.smartbrush.smartbrush_backend.entity.AuthEntity;
import com.smartbrush.smartbrush_backend.repository.AuthRepository;
import com.smartbrush.smartbrush_backend.repository.CommentRepository;
import com.smartbrush.smartbrush_backend.repository.CommunityRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
@Tag(name = "마이페이지 API", description = "마이페이지 API 입니다.")
public class MyPageController {

    private final CommunityRepository communityRepository;
    private final CommentRepository commentRepository;
    private final AuthRepository authRepository;

    @Operation(summary = "마이페이지 조회", description = "마이페이지를 조회합니다.")
    @GetMapping
    public ResponseEntity<MyPageResponseDTO> getMyPage(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String nickname = (String) request.getAttribute("author");
        String profileImage = (String) request.getAttribute("profileImage");

        AuthEntity user = authRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        MyPageResponseDTO dto = MyPageResponseDTO.builder()
                .nickname(nickname)
                .profileImage(profileImage)
                .attendanceDays(30)
                .email(user.getEmail())
                .build();

        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "마이페이지 수정", description = "마이페이지를 수정합니다.")
    @PatchMapping("/update")
    public ResponseEntity<Void> updateProfile(HttpServletRequest request,
                                              @RequestBody ProfileUpdateRequest updateRequest) {
        Long userId = (Long) request.getAttribute("userId");

        AuthEntity user = authRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        user.setNickname(updateRequest.getNickname());
        user.setEmail(updateRequest.getEmail());
        user.setProfileImage(updateRequest.getProfileImage());

        authRepository.save(user);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "내가 작성한 게시물 조회", description = "내가 작성한 게시물을 조회합니다.")
    @GetMapping("/posts")
    public ResponseEntity<List<CommunityResponseDTO>> getMyPosts(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        List<CommunityResponseDTO> posts = communityRepository.findByUserId(userId).stream()
                .map(c -> CommunityResponseDTO.builder()
                        .id(c.getId())
                        .title(c.getTitle())
                        .content(c.getContent())
                        .author(c.getAuthor())
                        .profileImage(c.getProfileImage())
                        .createdAt(c.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(posts);
    }

    @Operation(summary = "내가 작성한 댓글 조회", description = "내가 작성한 댓글을 조회합니다.")
    @GetMapping("/comments")
    public ResponseEntity<List<CommentResponseDTO>> getMyComments(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        List<CommentResponseDTO> comments = commentRepository.findByUserId(userId).stream()
                .map(c -> CommentResponseDTO.builder()
                        .id(c.getId())
                        .content(c.getContent())
                        .author(c.getAuthor())
                        .profileImage(c.getProfileImage())
                        .createdAt(c.getCreatedAt())
                        .isAuthor(true)
                        .postId(c.getCommunity().getId())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(comments);
    }
}

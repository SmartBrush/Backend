package com.smartbrush.smartbrush_backend.controller;

import com.smartbrush.smartbrush_backend.entity.Post;
import com.smartbrush.smartbrush_backend.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "my-page-controller", description = "마이페이지 API")
@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
public class MyPageController {

    private final PostService postService;

    @Operation(summary = "좋아요한 토픽 조회", description = "사용자가 좋아요를 누른 모든 토픽을 조회합니다.")
    @GetMapping("/likes")
    public ResponseEntity<List<Post>> likedPosts(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(postService.getLikedPosts(userDetails.getUsername()));
    }

    @Operation(summary = "스크랩한 토픽 조회", description = "사용자가 스크랩한 모든 토픽을 조회합니다.")
    @GetMapping("/scraps")
    public ResponseEntity<List<Post>> scrappedPosts(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(postService.getScrappedPosts(userDetails.getUsername()));
    }
}


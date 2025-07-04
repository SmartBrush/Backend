package com.smartbrush.smartbrush_backend.controller;

import com.smartbrush.smartbrush_backend.dto.post.PostRequestDto;
import com.smartbrush.smartbrush_backend.dto.post.PostResponseDto;
import com.smartbrush.smartbrush_backend.entity.Post;
import com.smartbrush.smartbrush_backend.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@Tag(name = "post-controller", description = "토픽 관련 API")
@RestController
@RequestMapping("/api/post")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @Operation(summary = "토픽 전체 조회", description = "작성된 모든 핫토픽 게시글을 조회합니다.")
    @GetMapping("/list")
    public ResponseEntity<List<PostResponseDto>> getAllPosts() {
        List<PostResponseDto> result = postService.getAllPosts();
        return ResponseEntity.ok(result);
    }



    @Operation(summary = "토픽 좋아요", description = "사용자가 특정 토픽에 좋아요를 누릅니다.")
    @PostMapping("/{id}/like")
    public ResponseEntity<?> likePost(@PathVariable Long id, @RequestAttribute String email) {
        postService.likePost(id, email);
        return ResponseEntity.ok().build();
    }


    @Operation(summary = "토픽 스크랩", description = "사용자가 특정 토픽을 스크랩합니다.")
    @PostMapping("/{id}/scrap")
    public ResponseEntity<Void> scrapPost(@PathVariable Long id,  @RequestAttribute String email) {
        postService.scrapPost(id,email);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "토픽 글 생성", description = "새로운 토픽 게시글을 작성합니다")
    @PostMapping("/create")
    public ResponseEntity<Void> createPost(@RequestBody PostRequestDto dto) {
        postService.createPost(dto);
        return ResponseEntity.ok().build();
    }

}

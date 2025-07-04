package com.smartbrush.smartbrush_backend.controller;

import com.smartbrush.smartbrush_backend.code.ResponseCode;
import com.smartbrush.smartbrush_backend.dto.comment.CommentRequestDTO;
import com.smartbrush.smartbrush_backend.dto.comment.CommentResponseDTO;
import com.smartbrush.smartbrush_backend.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "커뮤니티 고민공유 댓글", description = "커뮤니티 고민공유 댓글 기능")
@RequestMapping("/api/community")
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "댓글 작성", description = "특정 게시글에 댓글을 작성합니다.")
    @PostMapping("/{communityId}/comments")
    public ResponseEntity<CommentResponseDTO> createComment(@PathVariable Long communityId,
                                                            @RequestBody CommentRequestDTO dto,
                                                            @RequestAttribute Long userId,
                                                            @RequestAttribute String author,
                                                            @RequestAttribute String profileImage) {
        return ResponseEntity.status(ResponseCode.CREATED.getStatus())
                .body(commentService.createComment(communityId, dto, userId, author, profileImage));
    }

    @Operation(summary = "댓글 전체 조회", description = "특정 게시글의 모든 댓글을 조회합니다.")
    @GetMapping("/{communityId}/comments")
    public ResponseEntity<List<CommentResponseDTO>> getComments(@PathVariable Long communityId,
                                                                @RequestAttribute Long userId) {
        return ResponseEntity.status(ResponseCode.SUCCESS.getStatus())
                .body(commentService.getCommentsByCommunityId(communityId, userId));
    }

    @Operation(summary = "댓글 삭제", description = "댓글을 삭제합니다. 본인 댓글만 삭제할 수 있습니다.")
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId,
                                              @RequestAttribute Long userId) {
        commentService.deleteComment(commentId, userId);
        return ResponseEntity.status(ResponseCode.SUCCESS.getStatus()).build();
    }

    @Operation(summary = "댓글 수정", description = "댓글을 수정합니다. 본인 댓글만 수정할 수 있습니다.")
    @PutMapping("/comments/{commentId}")
    public ResponseEntity<CommentResponseDTO> updateComment(@PathVariable Long commentId,
                                                            @RequestBody CommentRequestDTO dto,
                                                            @RequestAttribute Long userId) {
        return ResponseEntity.status(ResponseCode.SUCCESS.getStatus())
                .body(commentService.updateComment(commentId, dto, userId));
    }
}

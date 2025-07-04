package com.smartbrush.smartbrush_backend.service;

import com.smartbrush.smartbrush_backend.dto.comment.CommentRequestDTO;
import com.smartbrush.smartbrush_backend.dto.comment.CommentResponseDTO;

import java.util.List;

public interface CommentService {
    CommentResponseDTO createComment(Long communityId, CommentRequestDTO dto, Long userId, String author, String profileImage);
    List<CommentResponseDTO> getCommentsByCommunityId(Long communityId, Long userId);
    void deleteComment(Long commentId, Long userId);
    CommentResponseDTO updateComment(Long commentId, CommentRequestDTO dto, Long userId);
}

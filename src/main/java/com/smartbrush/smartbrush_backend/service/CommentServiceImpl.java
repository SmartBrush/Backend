package com.smartbrush.smartbrush_backend.service;

import com.smartbrush.smartbrush_backend.code.ErrorCode;
import com.smartbrush.smartbrush_backend.dto.comment.CommentRequestDTO;
import com.smartbrush.smartbrush_backend.dto.comment.CommentResponseDTO;
import com.smartbrush.smartbrush_backend.entity.Comment;
import com.smartbrush.smartbrush_backend.entity.Community;
import com.smartbrush.smartbrush_backend.exception.CommentException;
import com.smartbrush.smartbrush_backend.exception.CommunityException;
import com.smartbrush.smartbrush_backend.repository.CommentRepository;
import com.smartbrush.smartbrush_backend.repository.CommunityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final CommunityRepository communityRepository;

    @Override
    public CommentResponseDTO createComment(Long communityId, CommentRequestDTO dto, Long userId, String author, String profileImage) {
        Community community = getCommunityEntity(communityId);
        Comment comment = new Comment(dto.getContent(), author, profileImage, userId, community);
        return toDTO(commentRepository.save(comment), userId);
    }

    @Override
    public List<CommentResponseDTO> getCommentsByCommunityId(Long communityId, Long userId) {
        Community community = getCommunityEntity(communityId);
        return commentRepository.findByCommunityOrderByCreatedAtAsc(community)
                .stream()
                .map(comment -> toDTO(comment, userId))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = getCommentEntity(commentId);
        if (!comment.getUserId().equals(userId)) {
            throw new CommentException(ErrorCode.FORBIDDEN);
        }
        commentRepository.delete(comment);
    }

    @Override
    public CommentResponseDTO updateComment(Long commentId, CommentRequestDTO dto, Long userId) {
        Comment comment = getCommentEntity(commentId);
        if (!comment.getUserId().equals(userId)) {
            throw new CommentException(ErrorCode.FORBIDDEN);
        }
        comment.update(dto.getContent());
        
        // DB 반영
        Comment updated = commentRepository.save(comment);
        return toDTO(updated, userId);
    }

    private CommentResponseDTO toDTO(Comment comment, Long currentUserId) {
        return CommentResponseDTO.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .author(comment.getAuthor())
                .profileImage(comment.getProfileImage())
                .createdAt(comment.getCreatedAt())
                .isAuthor(comment.getUserId().equals(currentUserId))
                .build();
    }

    private Community getCommunityEntity(Long id) {
        return communityRepository.findById(id)
                .orElseThrow(() -> new CommunityException(ErrorCode.POST_NOT_FOUND));
    }

    private Comment getCommentEntity(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new CommentException(ErrorCode.COMMENT_NOT_FOUND));
    }
}

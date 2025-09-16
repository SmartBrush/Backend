package com.smartbrush.smartbrush_backend.dto.comment;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CommentResponseDTO {
    private Long id;
    private String content;
    private String author;
    private String profileImage;
    private LocalDateTime createdAt;
    private boolean isAuthor;
    private Long postId;
}

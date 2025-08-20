package com.smartbrush.smartbrush_backend.dto.community;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CommunityResponseDTO {
    private Long id;
    private String title;
    private String content;
    private String author;
    private String profileImage;
    private LocalDateTime createdAt;
    private long likeCount;
    private boolean liked;
    private long commentCount;
}

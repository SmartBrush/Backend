package com.smartbrush.smartbrush_backend.dto.post;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class PostResponseDto {
    private Long id;
    private String title;
    private String author;
    private String content;
    private String imageUrl;
    private LocalDate createdDate;
    private List<String> tags;
}

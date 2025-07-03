package com.smartbrush.smartbrush_backend.dto.post;

import lombok.Getter;

import java.util.List;

@Getter
public class PostRequestDto {
    private String title;
    private String author;
    private String content;
    private String imageUrl;
    private List<String> tags;
}

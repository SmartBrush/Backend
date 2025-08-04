package com.smartbrush.smartbrush_backend.dto.uv;

import lombok.Data;

@Data
public class UvRequestDto {
    private int uv;
    private String state;     // 건성 / 보통 / 지성
    private String deviceId;
    private String image;     // Base64로 인코딩된 이미지
}

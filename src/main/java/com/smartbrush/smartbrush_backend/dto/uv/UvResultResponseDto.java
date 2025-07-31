package com.smartbrush.smartbrush_backend.dto.uv;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UvResultResponseDto {
    private Long id;
    private int uv;
    private String state;
    private String deviceId;
    private LocalDateTime timestamp;
    private Long userId;

}

package com.smartbrush.smartbrush_backend.dto.uv;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class UvResult {
    private int uv;
    private String state;
    private String deviceId;
    private LocalDateTime timestamp;

    public UvResult(int uv, String state, String deviceId) {
        this.uv = uv;
        this.state = state;
        this.deviceId = deviceId;
        this.timestamp = LocalDateTime.now();
    }
}
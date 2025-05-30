package com.smartbrush.smartbrush_backend.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum ResponseCode {
    ;
    private final HttpStatus status;
    private final String message;
}

package com.smartbrush.smartbrush_backend.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum ResponseCode {
    SUCCESS(HttpStatus.OK, "요청이 성공적으로 처리되었습니다."),
    CREATED(HttpStatus.CREATED, "성공적으로 생성되었습니다.");

    private final HttpStatus status;
    private final String message;
}

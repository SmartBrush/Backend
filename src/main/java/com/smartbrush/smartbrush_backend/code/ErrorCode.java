package com.smartbrush.smartbrush_backend.code;

import com.smartbrush.smartbrush_backend.dto.response.ErrorResponseDTO;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum ErrorCode {
    /**
     * 400 BAD_REQUEST - 잘못된 요청
     */
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),

    /**
     * 401 UNAUTHORIZED - 인증 실패
     */


    /**
     * 403 FORBIDDEN - 권한 없음
     */


    /**
     * 404 NOT_FOUND - 요청한 리소스를 찾을 수 없음
     */


    /**
     * 406 NOT_ACCEPTABLE - 허용되지 않는 요청 형식
     */


    /**
     * 409 CONFLICT - 요청 충돌
     */


    /**
     * 502 BAD_GATEWAY - 이트웨이 또는 프록시 서버 오류
     */

    SAMPLE_EXCEPTION(HttpStatus.BAD_REQUEST, "샘플 예외입니다."),

    /**
     * Auth
     */
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String message;

    public ErrorResponseDTO getReasonHttpStatus() {
        return ErrorResponseDTO.builder()
                .message(message)
                .status(status.value())
                .isSuccess(false)
                .error(this.name())
                .build()
                ;
    }
}

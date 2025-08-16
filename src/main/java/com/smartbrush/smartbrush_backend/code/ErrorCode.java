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
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "비밀번호와 비밀번호 확인이 일치하지 않습니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),

    /**
     * 401 UNAUTHORIZED - 인증 실패
     */
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다."),


    /**
     * 403 FORBIDDEN - 권한 없음
     */
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),


    /**
     * 404 NOT_FOUND - 요청한 리소스를 찾을 수 없음
     */
    // Auth
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다."),

    /**
     * 406 NOT_ACCEPTABLE - 허용되지 않는 요청 형식
     */


    /**
     * 409 CONFLICT - 요청 충돌
     */
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 이메일입니다."),


    /**
     * 502 BAD_GATEWAY - 이트웨이 또는 프록시 서버 오류
     */
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."),

    SAMPLE_EXCEPTION(HttpStatus.BAD_REQUEST, "샘플 예외입니다."),


    ;

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

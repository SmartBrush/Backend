package com.smartbrush.smartbrush_backend.exception;

import com.smartbrush.smartbrush_backend.code.ErrorCode;
import com.smartbrush.smartbrush_backend.dto.response.ErrorResponseDTO;

public class GlobalException extends RuntimeException {
    private ErrorCode code;

    public GlobalException(ErrorCode code) {
        this.code = code;
    }

    public ErrorResponseDTO getErrorReasonHttpStatus() {
        return this.code.getReasonHttpStatus();
    }
}

package com.smartbrush.smartbrush_backend.exception;

import com.smartbrush.smartbrush_backend.code.ErrorCode;

public class CommunityException extends GlobalException {
    public CommunityException(ErrorCode code) {
        super(code);
    }
}

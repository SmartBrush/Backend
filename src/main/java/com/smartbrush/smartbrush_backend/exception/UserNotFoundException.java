package com.smartbrush.smartbrush_backend.exception;

import com.smartbrush.smartbrush_backend.code.ErrorCode;

public class UserNotFoundException extends GlobalException {
    public UserNotFoundException(ErrorCode code) {
        super(code);
    }
}

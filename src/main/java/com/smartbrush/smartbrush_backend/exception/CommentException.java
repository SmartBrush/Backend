package com.smartbrush.smartbrush_backend.exception;

import com.smartbrush.smartbrush_backend.code.ErrorCode;

public class CommentException extends GlobalException {
    public CommentException(ErrorCode code) { super(code); }
}

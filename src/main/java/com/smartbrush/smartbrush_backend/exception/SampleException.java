package com.smartbrush.smartbrush_backend.exception;

import com.smartbrush.smartbrush_backend.code.ErrorCode;

public class SampleException extends GlobalException{
    public SampleException(ErrorCode code) {
        super(code);
    }
}

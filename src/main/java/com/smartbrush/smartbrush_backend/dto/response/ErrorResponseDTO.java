package com.smartbrush.smartbrush_backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.smartbrush.smartbrush_backend.code.ErrorCode;
import lombok.*;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ErrorResponseDTO {
    private Boolean isSuccess;
    private int status;
    private String error;
    private String message;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> errors;

    public ErrorResponseDTO(ErrorCode errorCode, Map<String, String> errors) {
        this.isSuccess = false;
        this.status = errorCode.getStatus().value();
        this.error = errorCode.getStatus().name();
        this.message = errorCode.getMessage();
        this.errors = errors;
    }
}

package com.smartbrush.smartbrush_backend.dto.Auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class AuthResponseDTO {
    @Getter
    @AllArgsConstructor
    public static class LoginResult {
        private String accessToken;
        private String refreshToken;
        private String nickname;
    }

    @Getter
    @AllArgsConstructor
    public static class SignupResult {
        private Long userId;
        private String email;
        private String nickname;
    }
}

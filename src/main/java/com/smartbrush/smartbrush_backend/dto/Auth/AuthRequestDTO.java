package com.smartbrush.smartbrush_backend.dto.Auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

public class AuthRequestDTO {
    @Getter
    public static class Signup {
        @Schema(description = "이메일", example = "example@naver.com")
        @Email(message = "유효한 이메일 형식이어야 합니다.")
        @NotBlank(message = "이메일은 필수입니다.")
        private String email;

        @NotBlank(message = "비밀번호는 필수입니다.")
        private String password;

        @NotBlank(message = "비밀번호 확인은 필수입니다.")
        private String passwordCheck;

        @NotBlank(message = "닉네임은 필수입니다.")
        private String nickname;
    }

    @Getter
    public static class Login {
        @Schema(description = "이메일", example = "example@naver.com")
        @Email(message = "유효한 이메일 형식이어야 합니다.")
        @NotBlank(message = "이메일은 필수입니다.")
        private String email;

        @NotBlank(message = "비밀번호는 필수입니다.")
        private String password;
    }
}

package com.smartbrush.smartbrush_backend.controller;

import com.smartbrush.smartbrush_backend.code.ResponseCode;
import com.smartbrush.smartbrush_backend.dto.Auth.AuthRequestDTO;
import com.smartbrush.smartbrush_backend.dto.Auth.AuthResponseDTO;
import com.smartbrush.smartbrush_backend.dto.response.ResponseDTO;
import com.smartbrush.smartbrush_backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "로그인", description = "일반 로그인 API")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/auth/signup")
    @Operation(summary = "회원가입", description = "회원가입 API 입니다.")
    public ResponseDTO<AuthResponseDTO.SignupResult> signup(@RequestBody @Valid AuthRequestDTO.Signup request) {
        return new ResponseDTO<>(ResponseCode.SUCCESS, authService.signup(request));
    }

    @PostMapping("/auth/login")
    @Operation(summary = "로그인", description = "로그인 API 입니다.")
    public ResponseDTO<AuthResponseDTO.LoginResult> login(@RequestBody @Valid AuthRequestDTO.Login request) {
        return new ResponseDTO<>(ResponseCode.SUCCESS, authService.login(request));
    }
}

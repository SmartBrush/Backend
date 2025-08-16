package com.smartbrush.smartbrush_backend.service;

import com.smartbrush.smartbrush_backend.code.ErrorCode;
import com.smartbrush.smartbrush_backend.dto.Auth.AuthRequestDTO;
import com.smartbrush.smartbrush_backend.dto.Auth.AuthResponseDTO;
import com.smartbrush.smartbrush_backend.entity.AuthEntity;
import com.smartbrush.smartbrush_backend.exception.GlobalException;
import com.smartbrush.smartbrush_backend.jwt.JwtProvider;
import com.smartbrush.smartbrush_backend.repository.AuthRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Override
    public AuthResponseDTO.SignupResult signup(AuthRequestDTO.Signup request) {
        if (authRepository.existsByEmail(request.getEmail())) {
            throw new GlobalException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        if (!request.getPassword().equals(request.getPasswordCheck())) {
            throw new GlobalException(ErrorCode.PASSWORD_MISMATCH);
        }

        AuthEntity auth = authRepository.save(AuthEntity.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .build());

        return new AuthResponseDTO.SignupResult(auth.getId(), auth.getEmail(), auth.getNickname());
    }

//    @Override
//    public AuthResponseDTO.LoginResult login(AuthRequestDTO.Login request) {
//        AuthEntity auth = authRepository.findByEmail(request.getEmail())
//                .orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));
//
//        if (!passwordEncoder.matches(request.getPassword(), auth.getPassword())) {
//            throw new GlobalException(ErrorCode.INVALID_PASSWORD);
//        }
//
//        String token = jwtProvider.createToken(auth.getEmail());
//
//        return new AuthResponseDTO.LoginResult(token, auth.getNickname());
//    }
    @Override
    @Transactional
    public AuthResponseDTO.LoginResult login(AuthRequestDTO.Login request) {
        AuthEntity auth = authRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getPassword(), auth.getPassword())) {
            throw new GlobalException(ErrorCode.INVALID_PASSWORD);
        }

        // access / refresh 동시 발급
        String accessToken = jwtProvider.createToken(auth.getEmail());
        String refreshToken = jwtProvider.createRefreshToken(auth.getEmail());

        // refreshToken 저장 (DB/Redis 중 DB 예시)
        auth.setRefreshToken(refreshToken);
        // 필요한 경우 refreshToken 만료일도 저장하도록 확장 가능 (auth.setRefreshTokenExpiry(...))

        return new AuthResponseDTO.LoginResult(accessToken, refreshToken, auth.getNickname());
    }
}

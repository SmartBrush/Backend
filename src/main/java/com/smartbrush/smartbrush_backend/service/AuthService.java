package com.smartbrush.smartbrush_backend.service;

import com.smartbrush.smartbrush_backend.dto.Auth.AuthRequestDTO;
import com.smartbrush.smartbrush_backend.dto.Auth.AuthResponseDTO;

public interface AuthService {
    AuthResponseDTO.SignupResult signup(AuthRequestDTO.Signup request);
    AuthResponseDTO.LoginResult login(AuthRequestDTO.Login request);
}

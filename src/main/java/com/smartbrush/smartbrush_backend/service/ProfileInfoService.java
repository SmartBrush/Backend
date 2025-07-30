package com.smartbrush.smartbrush_backend.service;

import com.smartbrush.smartbrush_backend.dto.question.ProfileInfoRequestDTO;
import com.smartbrush.smartbrush_backend.dto.question.UserProfileInfoResponseDTO;
import com.smartbrush.smartbrush_backend.entity.AuthEntity;

public interface ProfileInfoService {
    UserProfileInfoResponseDTO save(AuthEntity authEntity, ProfileInfoRequestDTO request);
    UserProfileInfoResponseDTO getProfileInfo(AuthEntity authEntity);

}

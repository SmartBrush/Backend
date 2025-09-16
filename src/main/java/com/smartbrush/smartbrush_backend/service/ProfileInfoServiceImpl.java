package com.smartbrush.smartbrush_backend.service;

import com.smartbrush.smartbrush_backend.dto.question.ProfileInfoRequestDTO;
import com.smartbrush.smartbrush_backend.dto.question.UserProfileInfoResponseDTO;
import com.smartbrush.smartbrush_backend.entity.AuthEntity;
import com.smartbrush.smartbrush_backend.entity.UserProfileInfo;
import com.smartbrush.smartbrush_backend.repository.UserProfileInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfileInfoServiceImpl implements ProfileInfoService {

    private final UserProfileInfoRepository userProfileInfoRepository;

    @Override
    @Transactional
    public UserProfileInfoResponseDTO save(AuthEntity authEntity, ProfileInfoRequestDTO request) {
        // 기존 데이터가 있다면 삭제
        userProfileInfoRepository.findByAuthEntity(authEntity)
                .ifPresent(userProfileInfoRepository::delete);


        // 새로 저장
        UserProfileInfo info = UserProfileInfo.builder()
                .authEntity(authEntity)
                .gender(request.getGender())
                .age(request.getAge())
                .hairLength(request.getHairLength())
                .dyedOrPermedRecently(request.getDyedOrPermedRecently())
                .familyHairLoss(request.getFamilyHairLoss())
                .wearHatFrequently(request.getWearHatFrequently())
                .uvExposureLevel(request.getUvExposureLevel())
                .washingFrequency(request.getWashingFrequency())
                .usingProducts(request.getUsingProducts())
                .eatingHabits(request.getEatingHabits())
                .scalpSymptoms(request.getScalpSymptoms())
                .sleepDuration(request.getSleepDuration())
                .sleepStartTime(request.getSleepStartTime())
                .build();

        userProfileInfoRepository.save(info);

        return new UserProfileInfoResponseDTO(
                true, // ✅ 프로필 존재
                info.getId(),
                authEntity.getNickname(),
                authEntity.getEmail(),
                info.getGender(),
                info.getAge(),
                info.getHairLength(),
                info.getDyedOrPermedRecently(),
                info.getFamilyHairLoss(),
                info.getWearHatFrequently(),
                info.getUvExposureLevel(),
                info.getWashingFrequency(),
                info.getUsingProducts().stream().map(Enum::name).toList(),
                info.getEatingHabits().stream().map(Enum::name).collect(Collectors.toSet()),
                info.getScalpSymptoms().stream().map(Enum::name).collect(Collectors.toSet()),
                info.getSleepDuration(),
                info.getSleepStartTime()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileInfoResponseDTO getProfileInfo(AuthEntity authEntity) {
        return userProfileInfoRepository.findByAuthEntity(authEntity)
                .map(info -> new UserProfileInfoResponseDTO(
                        true, // 프로필 유무
                        info.getId(),
                        authEntity.getNickname(),
                        authEntity.getEmail(),
                        info.getGender(),
                        info.getAge(),
                        info.getHairLength(),
                        info.getDyedOrPermedRecently(),
                        info.getFamilyHairLoss(),
                        info.getWearHatFrequently(),
                        info.getUvExposureLevel(),
                        info.getWashingFrequency(),
                        info.getUsingProducts().stream().map(Enum::name).toList(),
                        info.getEatingHabits().stream().map(Enum::name).collect(Collectors.toSet()),
                        info.getScalpSymptoms().stream().map(Enum::name).collect(Collectors.toSet()),
                        info.getSleepDuration(),
                        info.getSleepStartTime()
                ))
                // 없을 때 false로 내려감
                .orElse(UserProfileInfoResponseDTO.empty(authEntity.getNickname(), authEntity.getEmail()));
    }
}


package com.smartbrush.smartbrush_backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartbrush.smartbrush_backend.dto.MBTI.ScalpMbtiSummaryDTO;
import com.smartbrush.smartbrush_backend.entity.DiagnosisEntity;
import com.smartbrush.smartbrush_backend.repository.AuthRepository;
import com.smartbrush.smartbrush_backend.repository.DiagnosisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ScalpProfileServiceImpl {

    private final DiagnosisRepository diagnosisRepository;
    private final AuthRepository authRepository;
    private final ObjectMapper om = new ObjectMapper();

    public ScalpMbtiSummaryDTO getSummary(String email) {
        // 1) 닉네임 조회
        String nickname = authRepository.findByEmail(email)
                .map(u -> u.getNickname())
                .orElse(email);

        // 2) 오늘 결과 없으면 최신 1건
        DiagnosisEntity dx = diagnosisRepository.findByEmailAndDiagnosedDate(email, LocalDate.now())
                .orElseGet(() -> diagnosisRepository.findTopByEmailOrderByDiagnosedDateDesc(email).orElse(null));

        if (dx == null) {
            return ScalpMbtiSummaryDTO.builder()
                    .nickname(nickname)
                    .email(email)
                    .scalpMbti(null)
                    .diagnosedDate(null)
                    .build();
        }

        // 3) DB JSON에서 MBTI 읽기
        String mbti = null;
        try {
            Map<String, Object> result = om.readValue(dx.getResultJson(), Map.class);
            Object existing = result.get("scalpMbti");
            if (existing instanceof String s && !s.isBlank()) {
                mbti = s;
            }
        } catch (Exception ignored) {}

        // 4) DTO 반환
        return ScalpMbtiSummaryDTO.builder()
                .nickname(nickname)
                .email(email)
                .scalpMbti(mbti)
                .diagnosedDate(dx.getDiagnosedDate())
                .build();
    }
}

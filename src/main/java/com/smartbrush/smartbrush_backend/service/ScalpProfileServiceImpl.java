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
    private final ScalpMbtiServiceImpl scalpMbtiService;
    private final ObjectMapper om = new ObjectMapper();

    public ScalpMbtiSummaryDTO getSummary(String email, boolean backfillIfMissing) {
        // 사용자 이름(닉네임) 조회
        String nickname = authRepository.findByEmail(email)
                .map(u -> u.getNickname())
                .orElse(email);

        // 최근 진단 (오늘 없으면 최신 1건)
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

        String mbti = null;
        try {
            Map<String, Object> result = om.readValue(dx.getResultJson(), Map.class);
            Object existing = result.get("scalpMbti");
            if (existing instanceof String s && !s.isBlank()) {
                mbti = s;
            } else {
                int sensitivity = ((Number) result.getOrDefault("scalpSensitivityValue", 55)).intValue();
                int sebum       = ((Number) result.getOrDefault("sebumLevelValue", 55)).intValue();
                int scaling     = ((Number) result.getOrDefault("scalingValue", 55)).intValue();
                int density     = ((Number) result.getOrDefault("densityValue", 55)).intValue();
                int thickness   = ((Number) result.getOrDefault("poreSizeValue", 60)).intValue(); // inverted 밀도 기반

                Object rd = result.get("rawDiagnosis");
                if (rd instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Map<String, Object>> parsed = (Map<String, Map<String, Object>>) rd;
                    mbti = scalpMbtiService.getMbtiWithConfidence(
                            parsed, sensitivity, sebum, scaling, density, thickness
                    );
                } else {
                    // rawDiagnosis가 없던 과거 데이터 대비 안전 폴백
                    mbti = scalpMbtiService.getMbtiWithConfidence(
                            Map.of(), sensitivity, sebum, scaling, density, thickness
                    );
                }

                if (backfillIfMissing && mbti != null && !mbti.isBlank()) {
                    result.put("scalpMbti", mbti);
                    dx.updateResult(om.writeValueAsString(result));
                    diagnosisRepository.save(dx);
                }
            }
        } catch (Exception ignore) {}

        return ScalpMbtiSummaryDTO.builder()
                .nickname(nickname)
                .email(email)
                .scalpMbti(mbti)
                .diagnosedDate(dx.getDiagnosedDate())
                .build();
    }
}

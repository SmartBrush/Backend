package com.smartbrush.smartbrush_backend.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartbrush.smartbrush_backend.entity.DiagnosisEntity;
import com.smartbrush.smartbrush_backend.jwt.JwtProvider;
import com.smartbrush.smartbrush_backend.repository.DiagnosisRepository;
import com.smartbrush.smartbrush_backend.service.ScalpMbtiServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/diagnosis")
@Tag(name = "AI 두피 진단", description = "AI 진단 결과 API")
public class DiagnosisController {

    private final DiagnosisRepository diagnosisRepository;
    private final JwtProvider jwtProvider;
    private final ScalpMbtiServiceImpl scalpMbtiService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "두피 이미지 진단", description = "이미지를 업로드하면 AI 진단 결과를 반환합니다.")
    public ResponseEntity<?> diagnose(
            @RequestPart("image") MultipartFile image,
            HttpServletRequest request
    ) {
        try {
            // 이미지 -> ByteArrayResource 변환
            byte[] bytes = image.getBytes();
            ByteArrayResource resource = new ByteArrayResource(bytes) {
                @Override
                public String getFilename() {
                    return image.getOriginalFilename();
                }
            };

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("image", resource);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = new RestTemplate().postForEntity(
                    "http://54.180.149.92:8000/ai", requestEntity, String.class);

            // 응답 파싱
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Map<String, Object>> parsed = objectMapper.readValue(
                    response.getBody(), new TypeReference<>() {});

            // 계산 결과 → 저장 대상
            Map<String, Object> result = calculateDiagnosisResult(parsed);

            // MBTI 계산 및 포함
            int sensitivity = ((Number) result.getOrDefault("scalpSensitivityValue", 55)).intValue();
            int sebum       = ((Number) result.getOrDefault("sebumLevelValue", 55)).intValue();
            int scaling     = ((Number) result.getOrDefault("scalingValue", 55)).intValue();
            int density     = ((Number) result.getOrDefault("densityValue", 55)).intValue();
            int thickness   = ((Number) result.getOrDefault("poreSizeValue", 60)).intValue();

            String mbti = scalpMbtiService.getMbti(sensitivity, sebum, scaling, density, thickness);
            result.put("scalpMbti", mbti); // 응답/저장 모두에 포함

            String jsonString = objectMapper.writeValueAsString(result); // MBTI 포함 후 직렬화

            // 사용자 인증 및 토큰 처리
            String token = jwtProvider.resolveToken(request);
            if (token == null || !jwtProvider.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("유효하지 않은 토큰입니다.");
            }

            String email = jwtProvider.getEmail(token);

            // 진단 결과 저장 (오늘자 upsert)
            DiagnosisEntity existing = diagnosisRepository
                    .findByEmailAndDiagnosedDate(email, LocalDate.now())
                    .orElse(null);

            if (existing != null) {
                existing.updateResult(jsonString);
                diagnosisRepository.save(existing);
            } else {
                DiagnosisEntity newDiagnosis = new DiagnosisEntity(email, LocalDate.now(), jsonString);
                diagnosisRepository.save(newDiagnosis);
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("진단 실패: " + e.getMessage());
        }
    }

    // ===== 계산 유틸 =====

    private int getScoreFromClassIndex(Integer classIndex) {
        if (classIndex == null) return 55;
        return switch (classIndex) {
            case 0 -> 30;
            case 1, 2 -> 55;
            case 3 -> 80;
            default -> 55;
        };
    }

    private int getInvertedScoreFromClassIndex(Integer classIndex) {
        if (classIndex == null) return 55;
        return switch (classIndex) {
            case 0 -> 80;
            case 1, 2 -> 55;
            case 3 -> 30;
            default -> 55;
        };
    }

    private int average(Integer... values) {
        int sum = 0, count = 0;
        for (Integer v : values) if (v != null) { sum += v; count++; }
        return count == 0 ? 55 : Math.round((float) sum / count);
    }

    private String getStatusFromScore(double score) {
        if (score <= 4.0) return "심각";
        else if (score <= 6.5) return "보통";
        else return "양호";
    }

    private String getLabelFromScore(int value, String type) {
        return switch (type) {
            case "sebum" -> (value <= 30) ? "양호" : (value <= 55) ? "보통" : "심각";
            case "density" -> (value <= 30) ? "심각" : (value <= 55) ? "보통" : "양호";
            case "thickness" -> (value >= 60) ? "양호" : (value >= 40) ? "보통" : "심각";
            default -> (value >= 70) ? "심각" : (value >= 40) ? "보통" : "양호";
        };
    }

    private Map<String, Object> calculateDiagnosisResult(Map<String, Map<String, Object>> parsed) {
        Integer 민감도 = average(
                (Integer) parsed.getOrDefault("모낭사이홍반", Map.of()).get("class_index"),
                (Integer) parsed.getOrDefault("모낭홍반농포", Map.of()).get("class_index")
        );

        Integer 각질 = average(
                (Integer) parsed.getOrDefault("미세각질", Map.of()).get("class_index"),
                (Integer) parsed.getOrDefault("비듬", Map.of()).get("class_index")
        );

        Integer 탈모 = (Integer) parsed.getOrDefault("탈모", Map.of()).get("class_index");
        Integer 피지 = (Integer) parsed.getOrDefault("피지과다", Map.of()).get("class_index");

        int scalpSensitivityValue = getScoreFromClassIndex(민감도);
        int scalingValue = getScoreFromClassIndex(각질);
        int densityValue = getInvertedScoreFromClassIndex(탈모);
        int sebumLevelValue = getScoreFromClassIndex(피지);
        int poreSizeValue = 60; // 고정값

        int goodSensitivity = 100 - scalpSensitivityValue;
        int goodScaling = 100 - scalingValue;
        int goodSebum = 100 - sebumLevelValue;

        double rawAvg = (goodSensitivity + goodScaling + goodSebum + densityValue + poreSizeValue) / 5.0;
        double score = Math.round((rawAvg / 100.0) * 10 * 10) / 10.0;
        String status = getStatusFromScore(score);

        Map<String, Object> result = new HashMap<>();
        result.put("scalpSensitivityValue", scalpSensitivityValue);
        result.put("scalpSensitivityLevel", getLabelFromScore(scalpSensitivityValue, "sensitivity"));

        result.put("densityValue", densityValue);
        result.put("densityLevel", getLabelFromScore(densityValue, "density"));

        result.put("sebumLevelValue", sebumLevelValue);
        result.put("sebumLevel", getLabelFromScore(sebumLevelValue, "sebum"));

        result.put("poreSizeValue", poreSizeValue);
        result.put("poreSizeLevel", getLabelFromScore(poreSizeValue, "thickness"));

        result.put("scalingValue", scalingValue);
        result.put("scalingLevel", getLabelFromScore(scalingValue, "scaling"));

        result.put("score", score);
        result.put("status", status);
        result.put("rawDiagnosis", parsed);

        return result;
    }
}

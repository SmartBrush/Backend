package com.smartbrush.smartbrush_backend.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartbrush.smartbrush_backend.entity.DiagnosisEntity;
import com.smartbrush.smartbrush_backend.entity.DiagnosisImageEntity;
import com.smartbrush.smartbrush_backend.jwt.JwtProvider;
import com.smartbrush.smartbrush_backend.repository.DiagnosisRepository;
import com.smartbrush.smartbrush_backend.service.DiagnosisImageService;
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

import java.net.URI;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/diagnosis")
@Tag(name = "AI 두피 진단", description = "AI 진단 결과 API")
public class DiagnosisController {

    private final DiagnosisRepository diagnosisRepository;
    private final JwtProvider jwtProvider;
    private final ScalpMbtiServiceImpl scalpMbtiService;
    private final DiagnosisImageService diagnosisImageService;

    /**
     * 로그인 사용자 기준 최신 4장 이미지를 S3에서 불러와 Flask로 전달 → 결과 저장/반환
     */
    @PostMapping(value = "/upload")
    @Operation(
            summary = "두피 이미지 진단(최신 4장 자동 선택)",
            description = "로그인한 사용자의 최신 4장 이미지를 S3에서 불러와 AI 서버에 전송하고, 결과를 저장/반환합니다."
    )
    public ResponseEntity<?> diagnose(HttpServletRequest request) {
        try {
            // 1) 토큰 확인 및 이메일 추출
            String token = jwtProvider.resolveToken(request);
            if (token == null || !jwtProvider.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 토큰입니다.");
            }
            String email = jwtProvider.getEmail(token);

            // 2) 사용자 최신 4장 이미지 조회
            List<DiagnosisImageEntity> latestImages = diagnosisImageService.selectTop4Images(email);
            if (latestImages == null || latestImages.isEmpty()) {
                return ResponseEntity.badRequest().body("해당 사용자의 이미지가 존재하지 않습니다.");
            }

            // 3) S3 URL → 바이트 다운로드
            RestTemplate restTemplate = new RestTemplate();
            List<byte[]> imageBytes = latestImages.stream()
                    .map(DiagnosisImageEntity::getImageUrl)
                    .map(url -> fetchBytes(restTemplate, url))
                    .filter(Objects::nonNull)
                    .limit(4)
                    .collect(Collectors.toList());

            if (imageBytes.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("이미지 다운로드 실패");
            }

            // 4) Flask로 Multipart 전송
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            int idx = 1;
            for (byte[] bytes : imageBytes) {
                final String filename = "scalp_" + (idx++) + ".jpg";
                ByteArrayResource resource = new ByteArrayResource(bytes) {
                    @Override public String getFilename() { return filename; }
                };
                body.add("image", resource);
            }

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    URI.create("http://54.180.149.92:8000/ai"),
                    requestEntity,
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                        .body("AI 서버 오류: " + response.getStatusCode() + " - " + response.getBody());
            }

            // 5) Flask 응답 파싱
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> root = objectMapper.readValue(response.getBody(), new TypeReference<>() {});
            Object resultsObj = root.get("results");
            Map<String, Map<String, Object>> parsed =
                    objectMapper.convertValue(resultsObj, new TypeReference<Map<String, Map<String, Object>>>() {});

            // 6) 점수/라벨 계산 + MBTI
            Map<String, Object> result = calculateDiagnosisResult(parsed);

            // class_index(평균) 확보
            Integer sens = avgIdx(
                    (Integer) parsed.getOrDefault("모낭사이홍반", Map.of()).get("class_index"),
                    (Integer) parsed.getOrDefault("모낭홍반농포", Map.of()).get("class_index")
            );
            Integer scaling = avgIdx(
                    (Integer) parsed.getOrDefault("미세각질", Map.of()).get("class_index"),
                    (Integer) parsed.getOrDefault("비듬", Map.of()).get("class_index")
            );
            Integer sebum = (Integer) parsed.getOrDefault("피지과다", Map.of()).get("class_index");

            // MBTI 결정 (class_index 직결)
            String mbti = scalpMbtiService.getMbti(sens, sebum, scaling);
            result.put("scalpMbti", mbti);


            // 7) DB 저장(당일 한 건 유지/갱신)
            String jsonString = objectMapper.writeValueAsString(result);
            DiagnosisEntity existing = diagnosisRepository
                    .findByEmailAndDiagnosedDate(email, LocalDate.now())
                    .orElse(null);

            if (existing != null) {
                existing.updateResult(jsonString);
                diagnosisRepository.save(existing);
            } else {
                diagnosisRepository.save(new DiagnosisEntity(email, LocalDate.now(), jsonString));
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("진단 실패: " + e.getMessage());
        }
    }

    // ---- 내부 유틸 (S3 URL → byte[]) ----
    private byte[] fetchBytes(RestTemplate restTemplate, String url) {
        try {
            ResponseEntity<byte[]> res = restTemplate.getForEntity(url, byte[].class);
            if (res.getStatusCode().is2xxSuccessful() && res.getBody() != null) {
                return res.getBody();
            }
        } catch (Exception ignored) {}
        return null;
    }

    // ===== 점수/라벨 산출 =====
    private int getDetailedScore(Integer a, Integer b, String type) {
        if (a == null || b == null) return 55;

        // 0~3 범위 보정
        a = Math.max(0, Math.min(3, a));
        b = Math.max(0, Math.min(3, b));

        // 조합별 점수 매핑
        Map<String, Integer> sensitivityMap = Map.ofEntries(
                Map.entry("0,0", 30), Map.entry("0,1", 35), Map.entry("0,2", 40), Map.entry("0,3", 45),
                Map.entry("1,0", 40), Map.entry("1,1", 45), Map.entry("1,2", 50), Map.entry("1,3", 55),
                Map.entry("2,0", 50), Map.entry("2,1", 55), Map.entry("2,2", 60), Map.entry("2,3", 65),
                Map.entry("3,0", 60), Map.entry("3,1", 65), Map.entry("3,2", 70), Map.entry("3,3", 80)
        );

        Map<String, Integer> scalingMap = Map.ofEntries(
                Map.entry("0,0", 30), Map.entry("0,1", 33), Map.entry("0,2", 36), Map.entry("0,3", 40),
                Map.entry("1,0", 38), Map.entry("1,1", 42), Map.entry("1,2", 48), Map.entry("1,3", 52),
                Map.entry("2,0", 50), Map.entry("2,1", 55), Map.entry("2,2", 60), Map.entry("2,3", 65),
                Map.entry("3,0", 60), Map.entry("3,1", 66), Map.entry("3,2", 72), Map.entry("3,3", 80)
        );

        String key = a + "," + b;

        if (type.equals("sensitivity")) {
            return sensitivityMap.getOrDefault(key, 55);
        } else if (type.equals("scaling")) {
            return scalingMap.getOrDefault(key, 55);
        }
        return 55;
    }

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

    private Integer avgIdx(Integer a, Integer b) {
        if (a == null && b == null) return null;
        if (a == null) return b;
        if (b == null) return a;
        return (int) Math.round((a + b) / 2.0);
    }

    private String getStatusFromScore(double score) {
        // (원본 유지) 필요 시 프로젝트 기준에 맞춰 조절 가능
        if (score <= 3.0) return "심각";
        else if (score <= 6) return "보통";
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

    /** Flask 평균 결과 → 최종 지표/라벨/점수 변환 */
    private Map<String, Object> calculateDiagnosisResult(Map<String, Map<String, Object>> parsed) {
        Integer 모낭사이홍반 = (Integer) parsed.getOrDefault("모낭사이홍반", Map.of()).get("class_index");
        Integer 모낭홍반농포 = (Integer) parsed.getOrDefault("모낭홍반농포", Map.of()).get("class_index");
        Integer 미세각질 = (Integer) parsed.getOrDefault("미세각질", Map.of()).get("class_index");
        Integer 비듬 = (Integer) parsed.getOrDefault("비듬", Map.of()).get("class_index");
        Integer 탈모 = (Integer) parsed.getOrDefault("탈모", Map.of()).get("class_index");
        Integer 피지 = (Integer) parsed.getOrDefault("피지과다", Map.of()).get("class_index");
        Integer 모발밀도 = (Integer) parsed.getOrDefault("모발밀도", Map.of()).get("class_index");

        // 평균 class_index
        Integer 민감도 = average(모낭사이홍반, 모낭홍반농포);
        Integer 각질 = average(미세각질, 비듬);

        // 세분화 점수 (16조합용)
        int scalpSensitivityValue = getDetailedScore(모낭사이홍반, 모낭홍반농포, "sensitivity");
        int scalingValue          = getDetailedScore(미세각질, 비듬, "scaling");
        int densityValue          = getInvertedScoreFromClassIndex(탈모);
        int sebumLevelValue       = getScoreFromClassIndex(피지);
        int poreSizeValue         = getInvertedScoreFromClassIndex(모발밀도);

        // 이하 동일
        int goodSensitivity = 100 - scalpSensitivityValue;
        int goodScaling     = 100 - scalingValue;
        int goodSebum       = 100 - sebumLevelValue;

        double rawAvg = (goodSensitivity + goodScaling + goodSebum + densityValue + poreSizeValue) / 5.0;
        double score  = Math.round((rawAvg / 100.0) * 10 * 10) / 10.0;
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

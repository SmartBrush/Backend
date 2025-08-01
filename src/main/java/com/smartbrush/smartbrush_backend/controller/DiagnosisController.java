//package com.smartbrush.smartbrush_backend.controller;
//
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.smartbrush.smartbrush_backend.entity.DiagnosisEntity;
//import com.smartbrush.smartbrush_backend.jwt.JwtProvider;
//import com.smartbrush.smartbrush_backend.repository.DiagnosisRepository;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import jakarta.servlet.http.HttpServletRequest;
//import lombok.RequiredArgsConstructor;
//import org.springframework.core.io.ByteArrayResource;
//import org.springframework.http.*;
//import org.springframework.util.LinkedMultiValueMap;
//import org.springframework.util.MultiValueMap;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.client.RestTemplate;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.time.LocalDate;
//import java.util.HashMap;
//import java.util.Map;
//
//@RestController
//@RequiredArgsConstructor
//@RequestMapping("/api/diagnosis")
//@Tag(name = "AI 두피 진단", description = "AI 진단 결과 API")
//public class DiagnosisController {
//
//    private final DiagnosisRepository diagnosisRepository;
//    private final JwtProvider jwtProvider;
//
//    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    @Operation(summary = "두피 이미지 진단", description = "이미지를 업로드하면 AI 진단 결과를 반환합니다.")
//    public ResponseEntity<?> diagnose(
//            @RequestPart("image") MultipartFile image,
//            HttpServletRequest request
//    ) {
//        try {
//            // 🔁 이미지 -> ByteArrayResource 변환
//            byte[] bytes = image.getBytes();
//            ByteArrayResource resource = new ByteArrayResource(bytes) {
//                @Override
//                public String getFilename() {
//                    return image.getOriginalFilename();
//                }
//            };
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
//
//            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
//            body.add("image", resource);
//
//            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
//
//            ResponseEntity<String> response = new RestTemplate().postForEntity(
//                    "http://localhost:5000/ai", requestEntity, String.class);
//
//            // 응답 파싱
//            ObjectMapper objectMapper = new ObjectMapper();
//            Map<String, Map<String, Object>> parsed = objectMapper.readValue(response.getBody(), new TypeReference<>() {});
//            String jsonString = objectMapper.writeValueAsString(parsed);
//
//            // 사용자 인증 및 토큰 처리
//            String token = jwtProvider.resolveToken(request);
//            if (token == null || !jwtProvider.validateToken(token)) {
//                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                        .body("유효하지 않은 토큰입니다.");
//            }
//
//            String email = jwtProvider.getEmail(token);
//
//            // 진단 결과 저장
//            DiagnosisEntity existing = diagnosisRepository
//                    .findByEmailAndDiagnosedDate(email, LocalDate.now())
//                    .orElse(null);
//
//            if (existing != null) {
//                existing.updateResult(jsonString);
//                diagnosisRepository.save(existing);
//            } else {
//                DiagnosisEntity newDiagnosis = new DiagnosisEntity(email, LocalDate.now(), jsonString);
//                diagnosisRepository.save(newDiagnosis);
//            }
//
//            return ResponseEntity.ok(parsed);
//
//        } catch (Exception e) {
//            e.printStackTrace(); // 🔍 콘솔 로그 확인용
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("진단 실패: " + e.getMessage());
//        }
//    }
//}



//package com.smartbrush.smartbrush_backend.controller;
//
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.smartbrush.smartbrush_backend.entity.DiagnosisEntity;
//import com.smartbrush.smartbrush_backend.jwt.JwtProvider;
//import com.smartbrush.smartbrush_backend.repository.DiagnosisRepository;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import jakarta.servlet.http.HttpServletRequest;
//import lombok.RequiredArgsConstructor;
//import org.springframework.core.io.ByteArrayResource;
//import org.springframework.http.*;
//import org.springframework.util.LinkedMultiValueMap;
//import org.springframework.util.MultiValueMap;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.client.RestTemplate;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.time.LocalDate;
//import java.util.HashMap;
//import java.util.Map;
//
//@RestController
//@RequiredArgsConstructor
//@RequestMapping("/api/diagnosis")
//@Tag(name = "AI 두피 진단", description = "AI 진단 결과 API")
//public class DiagnosisController {
//
//    private final DiagnosisRepository diagnosisRepository;
//    private final JwtProvider jwtProvider;
//
//    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    @Operation(summary = "두피 이미지 진단", description = "이미지를 업로드하면 AI 진단 결과를 반환합니다.")
//    public ResponseEntity<?> diagnose(
//            @RequestPart("image") MultipartFile image,
//            HttpServletRequest request
//    ) {
//        try {
//            // 이미지 -> ByteArrayResource 변환
//            byte[] bytes = image.getBytes();
//            ByteArrayResource resource = new ByteArrayResource(bytes) {
//                @Override
//                public String getFilename() {
//                    return image.getOriginalFilename();
//                }
//            };
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
//
//            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
//            body.add("image", resource);
//
//            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
//
//            ResponseEntity<String> response = new RestTemplate().postForEntity(
//                    "http://localhost:5000/ai", requestEntity, String.class);
//
//            // 응답 파싱
//            ObjectMapper objectMapper = new ObjectMapper();
//            Map<String, Map<String, Object>> parsed = objectMapper.readValue(
//                    response.getBody(), new TypeReference<>() {});
//            String jsonString = objectMapper.writeValueAsString(parsed);
//
//            // 사용자 인증 및 토큰 처리
//            String token = jwtProvider.resolveToken(request);
//            if (token == null || !jwtProvider.validateToken(token)) {
//                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                        .body("유효하지 않은 토큰입니다.");
//            }
//
//            String email = jwtProvider.getEmail(token);
//
//            // 진단 결과 저장
//            DiagnosisEntity existing = diagnosisRepository
//                    .findByEmailAndDiagnosedDate(email, LocalDate.now())
//                    .orElse(null);
//
//            if (existing != null) {
//                existing.updateResult(jsonString);
//                diagnosisRepository.save(existing);
//            } else {
//                DiagnosisEntity newDiagnosis = new DiagnosisEntity(email, LocalDate.now(), jsonString);
//                diagnosisRepository.save(newDiagnosis);
//            }
//
//            // 계산 로직 적용
//            Map<String, Object> result = calculateDiagnosisResult(parsed);
//            return ResponseEntity.ok(result);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("진단 실패: " + e.getMessage());
//        }
//    }
//
//    // ✅ 계산 유틸 함수들
//
//    private int getScoreFromClassIndex(Integer classIndex) {
//        if (classIndex == null) return 55;
//        return switch (classIndex) {
//            case 0 -> 30; // 양호
//            case 1, 2 -> 55; // 보통
//            case 3 -> 80; // 심각
//            default -> 55;
//        };
//    }
//
//    private int getInvertedScoreFromClassIndex(Integer classIndex) {
//        if (classIndex == null) return 55;
//        return switch (classIndex) {
//            case 0 -> 80;
//            case 1, 2 -> 55;
//            case 3 -> 30;
//            default -> 55;
//        };
//    }
//
//    private int average(Integer... values) {
//        int sum = 0, count = 0;
//        for (Integer v : values) {
//            if (v != null) {
//                sum += v;
//                count++;
//            }
//        }
//        return count == 0 ? 55 : Math.round((float) sum / count);
//    }
//
//    private String getStatusFromScore(double score) {
//        if (score <= 4.0) return "심각";
//        else if (score <= 6.5) return "보통";
//        else return "양호";
//    }
//
//    private Map<String, Object> calculateDiagnosisResult(Map<String, Map<String, Object>> parsed) {
//        Integer 민감도 = average(
//                (Integer) parsed.getOrDefault("모낭사이홍반", Map.of()).get("class_index"),
//                (Integer) parsed.getOrDefault("모낭홍반농포", Map.of()).get("class_index")
//        );
//
//        Integer 각질 = average(
//                (Integer) parsed.getOrDefault("미세각질", Map.of()).get("class_index"),
//                (Integer) parsed.getOrDefault("비듬", Map.of()).get("class_index")
//        );
//
//        Integer 탈모 = (Integer) parsed.getOrDefault("탈모", Map.of()).get("class_index");
//        Integer 피지 = (Integer) parsed.getOrDefault("피지과다", Map.of()).get("class_index");
//
//        int scalpSensitivityValue = getScoreFromClassIndex(민감도);
//        int scalingValue = getScoreFromClassIndex(각질);
//        int densityValue = getInvertedScoreFromClassIndex(탈모);
//        int sebumLevelValue = getScoreFromClassIndex(피지);
//        int poreSizeValue = 60; // 고정값
//
//        int goodSensitivity = 100 - scalpSensitivityValue;
//        int goodScaling = 100 - scalingValue;
//        int goodSebum = 100 - sebumLevelValue;
//
//        double rawAvg = (goodSensitivity + goodScaling + goodSebum + densityValue + poreSizeValue) / 5.0;
//        double score = Math.round((rawAvg / 100.0) * 10 * 10) / 10.0;
//        String status = getStatusFromScore(score);
//
//        Map<String, Object> result = new HashMap<>();
//        result.put("scalpSensitivityValue", scalpSensitivityValue); //두피 민감도
//        result.put("densityValue", densityValue); //모발 밀도
//        result.put("sebumLevelValue", sebumLevelValue); //피지(유분) 정도
//        result.put("poreSizeValue", poreSizeValue); //모발 굵기
//        result.put("scalingValue", scalingValue); //각질,비듬
//        result.put("score", score);
//        result.put("status", status);
//        result.put("rawDiagnosis", parsed); // 원본 결과도 포함
//
//        return result;
//    }
//}


package com.smartbrush.smartbrush_backend.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartbrush.smartbrush_backend.entity.DiagnosisEntity;
import com.smartbrush.smartbrush_backend.jwt.JwtProvider;
import com.smartbrush.smartbrush_backend.repository.DiagnosisRepository;
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
                    "http://localhost:5000/ai", requestEntity, String.class);

            // 응답 파싱
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Map<String, Object>> parsed = objectMapper.readValue(
                    response.getBody(), new TypeReference<>() {});
            String jsonString = objectMapper.writeValueAsString(parsed);

            // 사용자 인증 및 토큰 처리
            String token = jwtProvider.resolveToken(request);
            if (token == null || !jwtProvider.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("유효하지 않은 토큰입니다.");
            }

            String email = jwtProvider.getEmail(token);

            // 진단 결과 저장
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

            // 계산 후 결과 반환
            Map<String, Object> result = calculateDiagnosisResult(parsed);
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
        for (Integer v : values) {
            if (v != null) {
                sum += v;
                count++;
            }
        }
        return count == 0 ? 55 : Math.round((float) sum / count);
    }

    private String getStatusFromScore(double score) {
        if (score <= 4.0) return "심각";
        else if (score <= 6.5) return "보통";
        else return "양호";
    }

    private String getLabelFromScore(int value, String type) {
        return switch (type) {
            case "sebum" -> {
                if (value <= 30) yield "양호";
                else if (value <= 55) yield "보통";
                else yield "심각";
            }
            case "density" -> {
                if (value <= 30) yield "심각";
                else if (value <= 55) yield "보통";
                else yield "양호";
            }
            case "thickness" -> { // 모발 굵기
                if (value >= 60) yield "양호";
                else if (value >= 40) yield "보통";
                else yield "심각";
            }
            default -> { // 두피 민감도, 각질 등 (낮을수록 양호)
                if (value >= 70) yield "심각";
                else if (value >= 40) yield "보통";
                else yield "양호";
            }
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


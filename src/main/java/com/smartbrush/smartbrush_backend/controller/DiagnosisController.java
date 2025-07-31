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
//import org.springframework.http.*;
//import org.springframework.util.LinkedMultiValueMap;
//import org.springframework.util.MultiValueMap;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.client.RestTemplate;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.time.LocalDate;
//import java.util.Map;
//
//@RestController
//@RequiredArgsConstructor
//@RequestMapping("/api/diagnosis")
//@Tag(name = " AI 두피 진단", description = "AI 진단 결과 API")
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
//            // Flask 서버로 이미지 전송
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
//
//            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
//            body.add("image", new MultipartInputStreamFileResource(image.getInputStream(), image.getOriginalFilename()));
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
//            // 사용자 정보
//            String token = jwtProvider.resolveToken(request);
//            if (token == null || !jwtProvider.validateToken(token)) {
//                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                        .body("유효하지 않은 토큰입니다.");
//            }
//
//            String email = jwtProvider.getEmail(token);
//
//
//            // 진단 결과 저장 (같은 날짜면 덮어쓰기)
//            DiagnosisEntity existing = diagnosisRepository
//                    .findByEmailAndDiagnosedDate(email, LocalDate.now())
//                    .orElse(null);
//
//            if (existing != null) {
//                existing.updateResult(jsonString); // 진단 결과 갱신
//                diagnosisRepository.save(existing);
//            } else {
//                DiagnosisEntity newDiagnosis = new DiagnosisEntity(email, LocalDate.now(), jsonString);
//                diagnosisRepository.save(newDiagnosis);
//            }
//
//            return ResponseEntity.ok(parsed);
//
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("진단 실패: " + e.getMessage());
//        }
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
            // 🔁 이미지 -> ByteArrayResource 변환
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
            Map<String, Map<String, Object>> parsed = objectMapper.readValue(response.getBody(), new TypeReference<>() {});
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

            return ResponseEntity.ok(parsed);

        } catch (Exception e) {
            e.printStackTrace(); // 🔍 콘솔 로그 확인용
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("진단 실패: " + e.getMessage());
        }
    }
}

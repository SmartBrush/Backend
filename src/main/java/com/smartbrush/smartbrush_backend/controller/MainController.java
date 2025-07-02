package com.smartbrush.smartbrush_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartbrush.smartbrush_backend.entity.DiagnosisEntity;
import com.smartbrush.smartbrush_backend.jwt.JwtProvider;
import com.smartbrush.smartbrush_backend.repository.DiagnosisRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "메인페이지", description = "메인페이지 API")
public class MainController {

    private final DiagnosisRepository diagnosisRepository;
    private final JwtProvider jwtProvider;

    @GetMapping("/main")
    @Operation(summary = "사용자의 두피 진단 결과", description = "사용자가 두피를 진단한 모든 결과를 반환합니다.")
    public ResponseEntity<?> getAllResults(HttpServletRequest request) {
        try {
            String token = jwtProvider.resolveToken(request);
            String email = jwtProvider.getEmail(token);

            List<DiagnosisEntity> records = diagnosisRepository.findAllByEmail(email);
            List<Map<String, Object>> resultList = new ArrayList<>();

            ObjectMapper mapper = new ObjectMapper();

            for (DiagnosisEntity record : records) {
                Map<String, Object> result = new HashMap<>();
                result.put("date", record.getDiagnosedDate().toString());
                result.put("result", mapper.readValue(record.getResultJson(), Map.class));
                resultList.add(result);
            }

            return ResponseEntity.ok(resultList);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("조회 실패: " + e.getMessage());
        }
    }
}

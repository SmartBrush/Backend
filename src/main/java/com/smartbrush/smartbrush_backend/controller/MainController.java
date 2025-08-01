package com.smartbrush.smartbrush_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartbrush.smartbrush_backend.entity.AuthEntity;
import com.smartbrush.smartbrush_backend.entity.DiagnosisEntity;
import com.smartbrush.smartbrush_backend.jwt.JwtProvider;
import com.smartbrush.smartbrush_backend.repository.AuthRepository;
import com.smartbrush.smartbrush_backend.repository.DiagnosisRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "메인페이지", description = "메인페이지 API")
public class MainController {

    private final DiagnosisRepository diagnosisRepository;
    private final JwtProvider jwtProvider;
    private final AuthRepository authRepository;

    @GetMapping("/main/diagnosis")
    @Operation(summary = "사용자의 두피 진단 결과", description = "사용자가 두피를 진단한 모든 결과를 반환합니다.")
    public ResponseEntity<?> getAllResults(HttpServletRequest request) {
        try {
            String token = jwtProvider.resolveToken(request);
            String email = jwtProvider.getEmail(token);

            List<DiagnosisEntity> records = diagnosisRepository.findAllByEmail(email);
            List<Map<String, Object>> resultList = new ArrayList<>();

            ObjectMapper mapper = new ObjectMapper();

            for (DiagnosisEntity record : records) {
                Map<String, Object> originalMap = mapper.readValue(record.getResultJson(), Map.class);
                Map<String, Object> result = new HashMap<>();
                result.put("date", record.getDiagnosedDate().toString());

                // 묶어서 정리
                Map<String, Object> grouped = new LinkedHashMap<>();
                grouped.put("scalpSensitivity", Map.of(
                        "value", originalMap.get("scalpSensitivityValue"),
                        "level", originalMap.get("scalpSensitivityLevel")
                ));
                grouped.put("density", Map.of(
                        "value", originalMap.get("densityValue"),
                        "level", originalMap.get("densityLevel")
                ));
                grouped.put("sebumLevel", Map.of(
                        "value", originalMap.get("sebumLevelValue"),
                        "level", originalMap.get("sebumLevel")
                ));
                grouped.put("poreSize", Map.of(
                        "value", originalMap.get("poreSizeValue"),
                        "level", originalMap.get("poreSizeLevel")
                ));
                grouped.put("scaling", Map.of(
                        "value", originalMap.get("scalingValue"),
                        "level", originalMap.get("scalingLevel")
                ));
                grouped.put("score", originalMap.get("score"));
                grouped.put("status", originalMap.get("status"));
                grouped.put("rawDiagnosis", originalMap.get("rawDiagnosis"));

                result.put("result", grouped);
                resultList.add(result);
            }

            return ResponseEntity.ok(resultList);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("조회 실패: " + e.getMessage());
        }
    }


//    @GetMapping("/main/diagnosis/today")
//    @Operation(summary = "오늘의 진단 결과", description = "오늘 날짜에 진단된 점수와 상태만 반환합니다.")
//    public ResponseEntity<?> getTodayDiagnosisSummary(HttpServletRequest request) {
//        try {
//            String token = jwtProvider.resolveToken(request);
//            String email = jwtProvider.getEmail(token);
//
//            // 오늘 날짜의 진단 결과 조회
//            Optional<DiagnosisEntity> todayOpt = diagnosisRepository.findByEmailAndDiagnosedDate(email, LocalDate.now());
//
//            if (todayOpt.isEmpty()) {
//                return ResponseEntity.ok(Map.of(
//                        "message", "오늘 진단 결과가 없습니다."
//                ));
//            }
//
//            ObjectMapper mapper = new ObjectMapper();
//            Map<String, Object> resultMap = mapper.readValue(todayOpt.get().getResultJson(), Map.class);
//
//            Map<String, Object> summary = new HashMap<>();
//            summary.put("score", resultMap.get("score"));
//            summary.put("status", resultMap.get("status"));
//
//            return ResponseEntity.ok(summary);
//
//        } catch (Exception e) {
//            return ResponseEntity.internalServerError().body("조회 실패: " + e.getMessage());
//        }
//    }

    @GetMapping("/main/diagnosis/today")
    @Operation(summary = "오늘의 진단 결과", description = "오늘 날짜에 진단된 점수, 상태, 사용자 이름, 날짜를 반환합니다.")
    public ResponseEntity<?> getTodayDiagnosisSummary(HttpServletRequest request) {
        try {
            String token = jwtProvider.resolveToken(request);
            String email = jwtProvider.getEmail(token);

            // 사용자 닉네임 조회
            Optional<AuthEntity> userOpt = authRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(404).body("사용자를 찾을 수 없습니다.");
            }
            String nickname = userOpt.get().getNickname();

            // 오늘 날짜의 진단 결과 조회
            Optional<DiagnosisEntity> todayOpt = diagnosisRepository.findByEmailAndDiagnosedDate(email, LocalDate.now());

            if (todayOpt.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                        "nickname", nickname,
                        "message", "오늘 진단 결과가 없습니다."
                ));
            }

            DiagnosisEntity todayDiagnosis = todayOpt.get();
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> resultMap = mapper.readValue(todayDiagnosis.getResultJson(), Map.class);

            Map<String, Object> summary = new HashMap<>();
            summary.put("nickname", nickname);
            summary.put("score", resultMap.get("score"));
            summary.put("status", resultMap.get("status"));
            summary.put("date", todayDiagnosis.getDiagnosedDate().toString()); // 날짜 추가

            return ResponseEntity.ok(summary);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("조회 실패: " + e.getMessage());
        }
    }
}

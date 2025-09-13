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


    @GetMapping("/main/diagnosis/status")
    @Operation(summary = "날짜별 status만 조회", description = "사용자의 모든 진단 기록에서 날짜와 status만 반환합니다.")
    public ResponseEntity<?> getOnlyStatusAll(HttpServletRequest request) {
        try {
            String token = jwtProvider.resolveToken(request);
            String email = jwtProvider.getEmail(token);

            List<DiagnosisEntity> records = diagnosisRepository.findAllByEmail(email);
            ObjectMapper mapper = new ObjectMapper();

            // 날짜 → status 슬림 리스트로 변환 + 날짜 오름차순
            List<Map<String, String>> body = records.stream()
                    .sorted(Comparator.comparing(DiagnosisEntity::getDiagnosedDate)) // 날짜 오름차순
                    .map(rec -> {
                        String status = "";
                        try {
                            Map<String, Object> m = mapper.readValue(rec.getResultJson(), Map.class);
                            Object s = m.get("status");
                            status = (s == null) ? "" : String.valueOf(s);
                        } catch (Exception ignored) {}

                        return Map.of(
                                "date", rec.getDiagnosedDate().toString(),
                                "status", status
                        );
                    })
                    .toList();

            return ResponseEntity.ok(body);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("조회 실패: " + e.getMessage());
        }
    }


    @GetMapping("/main/diagnosis/status/by-month")
    @Operation(
            summary = "월별 날짜별 status만 조회 (빈 날짜 포함)",
            description = "요청한 연/월의 모든 날짜에 대해 status를 반환합니다. 진단 없는 날은 빈 문자열로 내려갑니다."
    )
    public ResponseEntity<?> getOnlyStatusByMonth(
            HttpServletRequest request,
            @RequestParam int year,
            @RequestParam int month // 1~12
    ) {
        try {
            String token = jwtProvider.resolveToken(request);
            String email = jwtProvider.getEmail(token);

            LocalDate start = LocalDate.of(year, month, 1);
            LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

            // 해당 월 범위의 기록 조회
            List<DiagnosisEntity> records = diagnosisRepository
                    .findAllByEmailAndDiagnosedDateBetween(email, start, end);

            ObjectMapper mapper = new ObjectMapper();

            // 날짜별 status 맵 (동일 날짜 여러건 있으면 "마지막으로 본 값"으로 덮어쓰기)
            Map<LocalDate, String> statusByDate = new HashMap<>();
            for (DiagnosisEntity rec : records) {
                String status = "";
                try {
                    Map<String, Object> m = mapper.readValue(rec.getResultJson(), Map.class);
                    Object s = m.get("status");
                    status = (s == null) ? "" : String.valueOf(s);
                } catch (Exception ignored) {}

                statusByDate.put(rec.getDiagnosedDate(), status);
            }

            // 월 전체 날짜 채워 넣기 (없으면 "")
            List<Map<String, String>> filled = new ArrayList<>();
            for (LocalDate cur = start; !cur.isAfter(end); cur = cur.plusDays(1)) {
                filled.add(Map.of(
                        "date", cur.toString(),
                        "status", statusByDate.getOrDefault(cur, "")
                ));
            }

            return ResponseEntity.ok(filled);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("조회 실패: " + e.getMessage());
        }
    }

    @GetMapping("/main/diagnosis/{date}")
    @Operation(summary = "지정 날짜의 진단 결과 조회", description = "특정 날짜에 대한 모든 진단 결과를 반환합니다.(2025-09-01)")
    public ResponseEntity<?> getDiagnosisByDate(HttpServletRequest request, @PathVariable String date) {
        try {
            String token = jwtProvider.resolveToken(request);
            String email = jwtProvider.getEmail(token);

            // 날짜 문자열을 LocalDate로 변환
            LocalDate diagnosedDate = LocalDate.parse(date);

            // 해당 날짜의 진단 결과 모두 조회
            List<DiagnosisEntity> records = diagnosisRepository.findAllByEmailAndDiagnosedDate(email, diagnosedDate);

            if (records.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                        "message", "해당 날짜에 진단된 결과가 없습니다."
                ));
            }

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

    @GetMapping("/main/diagnosis/average")
    @Operation(
            summary = "이전 3개월의 진단 결과 월별 평균",
            description = "이번 달을 제외한 이전 3개월의 각 월별로 각 요소별 평균을 구합니다."
    )
    public ResponseEntity<?> getAverageByMonthForLast3Months(HttpServletRequest request) {
        try {
            String token = jwtProvider.resolveToken(request);
            String email = jwtProvider.getEmail(token);

            // 이번 달 제외한 3개월 기간 (예: 6월, 7월, 8월)
            LocalDate today = LocalDate.now();
            LocalDate startOfMonth = today.withDayOfMonth(1);
            LocalDate startOfThreeMonthsAgo = startOfMonth.minusMonths(3);

            // 진단 기록 조회
            List<DiagnosisEntity> records = diagnosisRepository.findAllByEmailAndDiagnosedDateBetween(
                    email, startOfThreeMonthsAgo, startOfMonth.minusDays(1) // 이번 달 제외
            );

            if (records.isEmpty()) {
                return ResponseEntity.ok(Map.of("message", "이전 3개월 동안 진단된 결과가 없습니다."));
            }

            ObjectMapper mapper = new ObjectMapper();

            // 각 월별로 값을 저장할 Map
            Map<String, Map<String, Double>> monthlySumValues = new HashMap<>();
            Map<String, Map<String, Integer>> monthlyCountValues = new HashMap<>();

            // 각 진단 기록에서 요소별 값을 추출하여 월별로 평균 계산
            for (DiagnosisEntity record : records) {
                Map<String, Object> resultMap = mapper.readValue(record.getResultJson(), Map.class);
                LocalDate diagnosedDate = record.getDiagnosedDate();
                String monthKey = diagnosedDate.getYear() + "-" + diagnosedDate.getMonthValue(); // "2025-06"

                // 각 항목별 value 값을 추출하여 평균 계산
                for (String key : Arrays.asList("scalpSensitivity", "density", "sebumLevel", "poreSize", "scaling")) {
                    String valueKey = key + "Value"; // 예: scalpSensitivityValue

                    if (resultMap.containsKey(valueKey)) {
                        Object value = resultMap.get(valueKey);

                        // 값이 Integer일 경우 Double로 변환
                        double numericValue = 0;
                        if (value instanceof Integer) {
                            numericValue = ((Integer) value).doubleValue(); // Integer를 Double로 변환
                        } else if (value instanceof Double) {
                            numericValue = (Double) value; // 이미 Double이면 그대로 사용
                        }

                        // 월별 sum과 count 업데이트
                        monthlySumValues.putIfAbsent(monthKey, new HashMap<>());
                        monthlyCountValues.putIfAbsent(monthKey, new HashMap<>());
                        monthlySumValues.get(monthKey).put(valueKey, monthlySumValues.get(monthKey).getOrDefault(valueKey, 0.0) + numericValue);
                        monthlyCountValues.get(monthKey).put(valueKey, monthlyCountValues.get(monthKey).getOrDefault(valueKey, 0) + 1);
                    }
                }
            }

            // 각 월별 평균 계산
            Map<String, Map<String, Double>> monthlyAverages = new HashMap<>();
            for (String monthKey : monthlySumValues.keySet()) {
                Map<String, Double> monthSumValues = monthlySumValues.get(monthKey);
                Map<String, Integer> monthCountValues = monthlyCountValues.get(monthKey);
                Map<String, Double> monthAverages = new HashMap<>();

                for (String key : monthSumValues.keySet()) {
                    double sum = monthSumValues.get(key);
                    int count = monthCountValues.get(key);
                    double average = Math.round((sum / count) * 100.0) / 100.0;
                    monthAverages.put(key, average);
                }

                monthlyAverages.put(monthKey, monthAverages);
            }

            return ResponseEntity.ok(monthlyAverages);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("조회 실패: " + e.getMessage());
        }
    }



}

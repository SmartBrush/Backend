package com.smartbrush.smartbrush_backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CaptureController {

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/capture")
    public ResponseEntity<String> triggerEsp32Capture(HttpServletRequest request) {
        // ESP32의 /capture 엔드포인트 주소
        String esp32Url = "http://172.20.10.3/capture";

        try {
            // JWT 토큰 추출
            String jwtToken = extractJwtFromRequest(request);

            // 헤더에 토큰 추가
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + jwtToken);
            headers.setAccept(List.of(MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON));

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            // ESP32에 GET 요청 보내기
            ResponseEntity<String> response = restTemplate.exchange(
                    esp32Url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            return ResponseEntity.ok("📷 ESP32에게 캡처 요청 완료: " + response.getBody());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("⚠️ ESP32 캡처 요청 실패: " + e.getMessage());
        }
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        Object userId = request.getAttribute("userId");
        if (userId != null) {
            // 테스트용으로 헤더 없을 때도 동작하도록 처리
            return "test-token";
        }
        throw new RuntimeException("JWT 토큰이 필요합니다.");
    }
}

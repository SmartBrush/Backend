package com.smartbrush.smartbrush_backend.controller;

import com.smartbrush.smartbrush_backend.repository.AuthRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/device")
public class DeviceController {

    private final AuthRepository authRepository;

    @PostMapping("/send-token")
    public ResponseEntity<String> sendTokenToDevice(@RequestParam String deviceIp, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body("JWT 토큰 누락");
        }

        try {
            // 예: ESP32는 http://192.168.0.101/receive-token 주소로 받음
            String url = "http://" + deviceIp + "/receive-token";
            HttpClient client = HttpClient.newHttpClient();

            String json = "{\"token\":\"" + token.substring(7) + "\"}";

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            return ResponseEntity.ok("하드웨어 응답: " + response.body());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("토큰 전달 실패: " + e.getMessage());
        }
    }
}

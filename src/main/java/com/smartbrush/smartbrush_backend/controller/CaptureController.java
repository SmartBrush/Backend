package com.smartbrush.smartbrush_backend.controller;

import com.smartbrush.smartbrush_backend.entity.DiagnosisImageEntity;
import com.smartbrush.smartbrush_backend.jwt.JwtProvider;
import com.smartbrush.smartbrush_backend.repository.DiagnosisImageRepository;
import com.smartbrush.smartbrush_backend.storage.S3Uploader;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

//@RestController
//@RequiredArgsConstructor
//@RequestMapping("/api")
//public class CaptureController {
//
//    private final RestTemplate restTemplate = new RestTemplate();
//
//    @GetMapping("/capture")
//    public ResponseEntity<String> triggerEsp32Capture(HttpServletRequest request) {
//        // ESP32의 /capture 엔드포인트 주소
//        String esp32Url = "http://172.20.10.3/capture";
//
//        try {
//            // JWT 토큰 추출
//            String jwtToken = extractJwtFromRequest(request);
//
//            // 헤더에 토큰 추가
//            HttpHeaders headers = new HttpHeaders();
//            headers.set("Authorization", "Bearer " + jwtToken);
//            headers.setAccept(List.of(MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON));
//
//            HttpEntity<Void> entity = new HttpEntity<>(headers);
//
//            // ESP32에 GET 요청 보내기
//            ResponseEntity<String> response = restTemplate.exchange(
//                    esp32Url,
//                    HttpMethod.GET,
//                    entity,
//                    String.class
//            );
//
//            return ResponseEntity.ok("📷 ESP32에게 캡처 요청 완료: " + response.getBody());
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.status(500).body("⚠️ ESP32 캡처 요청 실패: " + e.getMessage());
//        }
//    }
//
//    private String extractJwtFromRequest(HttpServletRequest request) {
//        String authHeader = request.getHeader("Authorization");
//        if (authHeader != null && authHeader.startsWith("Bearer ")) {
//            return authHeader.substring(7);
//        }
//        Object userId = request.getAttribute("userId");
//        if (userId != null) {
//            // 테스트용으로 헤더 없을 때도 동작하도록 처리
//            return "test-token";
//        }
//        throw new RuntimeException("JWT 토큰이 필요합니다.");
//    }
//}


//@RestController
//@RequiredArgsConstructor
//@RequestMapping("/api")
//public class CaptureController {
//
//    private final RestTemplate restTemplate = new RestTemplate();
//    private final JwtProvider jwtProvider; // ✅ 사용자 식별 위해 추가
//    private final DiagnosisImageRepository diagnosisImageRepository; // ✅ 이미지 저장용 가정
//
//    private static final String ESP32_CAPTURE_URL = "http://172.20.10.11/capture";
//
//    @GetMapping("/capture")
//    public ResponseEntity<String> triggerEsp32RepeatedCapture(HttpServletRequest request) {
//        String token = extractJwtFromRequest(request);
//
//        // 사용자 정보 파싱
//        String email;
//        try {
//            email = jwtProvider.getEmail(token);
//        } catch (Exception e) {
//            return ResponseEntity.status(401).body("❌ 토큰 오류: " + e.getMessage());
//        }
//
//        // 비동기 캡처 쓰레드 시작
//        new Thread(() -> runCaptureLoop(email, token)).start();
//
//        return ResponseEntity.ok("🕐 1초 간격으로 100장 캡처 시작됨");
//    }
//
//    @GetMapping("/images/{id}")
//    public ResponseEntity<byte[]> getImage(@PathVariable Long id) {
//        DiagnosisImageEntity image = diagnosisImageRepository.findById(id)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
//
//        return ResponseEntity.ok()
//                .contentType(MediaType.IMAGE_JPEG)
//                .body(image.getImageData());
//    }
//
//    private void runCaptureLoop(String email, String token) {
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Authorization", "Bearer " + token);
//        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
//
//        for (int i = 0; i < 100; i++) {
//            try {
//                HttpEntity<Void> entity = new HttpEntity<>(headers);
//                ResponseEntity<byte[]> response = restTemplate.exchange(
//                        ESP32_CAPTURE_URL,
//                        HttpMethod.GET,
//                        entity,
//                        byte[].class
//                );
//
//                if (response.getStatusCode().is2xxSuccessful()) {
//                    byte[] imageData = response.getBody();
//
//                    // ✅ DB에 저장 (DiagnosisImageEntity 가정)
//                    DiagnosisImageEntity image = new DiagnosisImageEntity();
//                    image.setEmail(email);
//                    image.setCapturedAt(LocalDateTime.now());
//                    image.setImageData(imageData); // BLOB or base64 encoding
//                    diagnosisImageRepository.save(image);
//                }
//
//                Thread.sleep(1000); // 1초 대기
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    private String extractJwtFromRequest(HttpServletRequest request) {
//        String authHeader = request.getHeader("Authorization");
//        if (authHeader != null && authHeader.startsWith("Bearer ")) {
//            return authHeader.substring(7);
//        }
//        return "test-token"; // 테스트용
//    }
//}

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CaptureController {

    private final RestTemplate restTemplate = new RestTemplate();
    private final JwtProvider jwtProvider;
    private final DiagnosisImageRepository diagnosisImageRepository;
    private final S3Uploader s3Uploader; // ✅ 추가

    private static final String ESP32_CAPTURE_URL = "http://172.20.10.11/capture";

    @GetMapping("/capture")
    public ResponseEntity<String> triggerEsp32RepeatedCapture(HttpServletRequest request) {
        String token = extractJwtFromRequest(request);
        String email = jwtProvider.getEmail(token);

        new Thread(() -> runCaptureLoop(email, token)).start();

        return ResponseEntity.ok("🕐 1초 간격으로 100장 S3에 업로드 시작됨");
    }

    private void runCaptureLoop(String email, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        for (int i = 0; i < 100; i++) {
            try {
                HttpEntity<Void> entity = new HttpEntity<>(headers);
                ResponseEntity<byte[]> response = restTemplate.exchange(
                        ESP32_CAPTURE_URL, HttpMethod.GET, entity, byte[].class
                );

                if (response.getStatusCode().is2xxSuccessful()) {
                    byte[] imageData = response.getBody();
                    String fileName = "diagnosis/" + email + "/" + UUID.randomUUID() + ".jpg";

                    String imageUrl = s3Uploader.upload(imageData, fileName);

                    DiagnosisImageEntity image = new DiagnosisImageEntity();
                    image.setEmail(email);
                    image.setCapturedAt(LocalDateTime.now());
                    image.setImageUrl(imageUrl);
                    diagnosisImageRepository.save(image);
                }

                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return "test-token";
    }

    @GetMapping("/images/{id}")
    public ResponseEntity<String> getImageUrl(@PathVariable Long id) {
        DiagnosisImageEntity image = diagnosisImageRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return ResponseEntity.ok(image.getImageUrl());
    }

}

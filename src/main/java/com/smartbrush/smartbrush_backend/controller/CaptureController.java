package com.smartbrush.smartbrush_backend.controller;

import com.smartbrush.smartbrush_backend.entity.DiagnosisImageEntity;
import com.smartbrush.smartbrush_backend.jwt.JwtProvider;
import com.smartbrush.smartbrush_backend.repository.DiagnosisImageRepository;
import com.smartbrush.smartbrush_backend.service.DiagnosisImageService;
import com.smartbrush.smartbrush_backend.storage.S3Uploader;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.RequestBody; // ✅ 이걸로 변경
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CaptureController {

    private final RestTemplate restTemplate = new RestTemplate();
    private final JwtProvider jwtProvider;
    private final DiagnosisImageRepository diagnosisImageRepository;
    private final S3Uploader s3Uploader;

    private final DiagnosisImageService diagnosisImageService;


    private static final String ESP32_CAPTURE_URL = "http://172.20.10.3/capture";

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

    @GetMapping("/images/top4")
    public ResponseEntity<List<String>> getTop4Images(HttpServletRequest request) {
        String token = extractJwtFromRequest(request);
        String email = jwtProvider.getEmail(token);

        System.out.println("[요청 이메일]: " + email);

        List<DiagnosisImageEntity> topImages = diagnosisImageService.selectTop4Images(email);

        System.out.println("[조회된 개수]: " + topImages.size());

        List<String> urls = topImages.stream()
                .map(DiagnosisImageEntity::getImageUrl)
                .toList();

        return ResponseEntity.ok(urls);
    }


//    @PostMapping("/image/upload")
//    public ResponseEntity<String> receiveImage(
//            @RequestBody byte[] imageData, HttpServletRequest request) {
//
//        String token = extractJwtFromRequest(request);
//        String email = jwtProvider.getEmail(token);
//
//        String fileName = "diagnosis/" + email + "/" + UUID.randomUUID() + ".jpg";
//        String imageUrl = s3Uploader.upload(imageData, fileName);
//
//        DiagnosisImageEntity image = new DiagnosisImageEntity();
//        image.setEmail(email);
//        image.setCapturedAt(LocalDateTime.now());
//        image.setImageUrl(imageUrl);
//        diagnosisImageRepository.save(image);
//
//        return ResponseEntity.ok("S3 업로드 완료: " + imageUrl);
//    }

//    @Operation(
//            summary = "이미지 업로드 (byte[])",
//            description = "ESP32-CAM에서 찍은 이미지를 JWT 기반으로 인증하여 S3에 업로드합니다."
//    )
//    @PostMapping("/image/upload")
//    public ResponseEntity<String> receiveImage(@RequestBody byte[] imageData, HttpServletRequest request) {
//        String token = extractJwtFromRequest(request);
//        String email = jwtProvider.getEmail(token); // 이메일 기준
//
//        String fileName = "diagnosis/" + email + "/" + UUID.randomUUID() + ".jpg";
//        String imageUrl = s3Uploader.upload(imageData, fileName);
//
//        DiagnosisImageEntity image = new DiagnosisImageEntity();
//        image.setEmail(email);
//        image.setCapturedAt(LocalDateTime.now());
//        image.setImageUrl(imageUrl);
//        diagnosisImageRepository.save(image);
//
//        return ResponseEntity.ok("✅ S3 업로드 성공: " + imageUrl);
//    }

    @Operation(
            summary = "이미지 업로드 (ESP32 → S3)",
            description = "ESP32-CAM에서 전송된 JPEG 이미지를 S3에 저장합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody( // ⬅ Swagger용
                    required = true,
                    content = @Content(
                            mediaType = "application/octet-stream",
                            schema = @Schema(type = "string", format = "binary")
                    )
            )
    )
    @PostMapping("/image/upload")
    public ResponseEntity<String> receiveImage(
            @org.springframework.web.bind.annotation.RequestBody byte[] imageData, // ⬅ Spring의 RequestBody
            HttpServletRequest request) {

        String token = extractJwtFromRequest(request);
        String email = jwtProvider.getEmail(token);

        String fileName = "diagnosis/" + email + "/" + UUID.randomUUID() + ".jpg";
        String imageUrl = s3Uploader.upload(imageData, fileName);

        DiagnosisImageEntity image = new DiagnosisImageEntity();
        image.setEmail(email);
        image.setCapturedAt(LocalDateTime.now());
        image.setImageUrl(imageUrl);
        diagnosisImageRepository.save(image);

        return ResponseEntity.ok("✅ S3 업로드 성공: " + imageUrl);
    }

}

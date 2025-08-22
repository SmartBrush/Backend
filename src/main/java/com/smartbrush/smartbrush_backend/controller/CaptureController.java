package com.smartbrush.smartbrush_backend.controller;

import com.smartbrush.smartbrush_backend.entity.DiagnosisImageEntity;
import com.smartbrush.smartbrush_backend.jwt.JwtProvider;
import com.smartbrush.smartbrush_backend.repository.DiagnosisImageRepository;
import com.smartbrush.smartbrush_backend.service.DiagnosisImageService;
import com.smartbrush.smartbrush_backend.storage.S3Uploader;
import io.swagger.v3.oas.annotations.Operation;
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

    // 내부 망으로 ESP32에서 캡처 이미지를 제공하는 엔드포인트(필요 시 수정)
    private static final String ESP32_CAPTURE_URL = "http://172.20.10.14/capture";

    @GetMapping("/capture")
    public ResponseEntity<String> triggerEsp32RepeatedCapture(HttpServletRequest request) {
        String token = extractJwtFromRequest(request);
        String email = jwtProvider.getEmail(token);
        new Thread(() -> runCaptureLoop(email, token)).start();
        return ResponseEntity.ok("1초 간격 100장 S3 업로드 시작");
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

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    byte[] imageData = response.getBody();
                    String fileName = "diagnosis/" + email + "/" + UUID.randomUUID() + ".jpg";
                    String imageUrl = s3Uploader.upload(imageData, fileName, "image/jpeg");

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
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authorization 헤더 누락");
    }

    @GetMapping("/images/top4")
    public ResponseEntity<List<String>> getTop4Images(HttpServletRequest request) {
        String token = extractJwtFromRequest(request);
        String email = jwtProvider.getEmail(token);
        List<DiagnosisImageEntity> topImages = diagnosisImageService.selectTop4Images(email);
        List<String> urls = topImages.stream().map(DiagnosisImageEntity::getImageUrl).toList();
        return ResponseEntity.ok(urls);
    }

    @Operation(
            summary = "이미지 업로드 (ESP32 → S3, RAW 바이트)",
            description = "ESP32-CAM이 전송한 JPEG 바이트를 그대로 받아 S3에 저장합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/octet-stream",
                            schema = @Schema(type = "string", format = "binary")
                    )
            )
    )
    @PostMapping(
            value = "/image/upload",
            consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE
    )
    public ResponseEntity<String> receiveImage(@RequestBody byte[] imageData,
                                               HttpServletRequest request) {
        String token = extractJwtFromRequest(request);
        String email = jwtProvider.getEmail(token);

        if (imageData == null || imageData.length == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "빈 바디");
        }

        String fileName = "diagnosis/" + email + "/" + UUID.randomUUID() + ".jpg";
        String imageUrl = s3Uploader.upload(imageData, fileName, "image/jpeg");

        DiagnosisImageEntity image = new DiagnosisImageEntity();
        image.setEmail(email);
        image.setCapturedAt(LocalDateTime.now());
        image.setImageUrl(imageUrl);
        diagnosisImageRepository.save(image);

        return ResponseEntity.ok("✅ S3 업로드 성공: " + imageUrl);
    }
}

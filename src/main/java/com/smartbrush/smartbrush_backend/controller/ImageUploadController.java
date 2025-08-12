package com.smartbrush.smartbrush_backend.controller;

import com.smartbrush.smartbrush_backend.entity.DiagnosisImageEntity;
import com.smartbrush.smartbrush_backend.jwt.JwtProvider;
import com.smartbrush.smartbrush_backend.repository.DiagnosisImageRepository;
import com.smartbrush.smartbrush_backend.storage.S3Uploader;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;

@Tag(name = "Image Upload")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/image")
public class ImageUploadController {

    private final JwtProvider jwtProvider;
    private final DiagnosisImageRepository diagnosisImageRepository;
    private final S3Uploader s3Uploader;

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
            value = "/upload",
            consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE
    )
    public ResponseEntity<String> upload(@RequestBody byte[] imageData,
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

        return ResponseEntity.ok("S3 업로드 성공: " + imageUrl);
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authorization 헤더 누락");
    }
}

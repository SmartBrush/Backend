package com.smartbrush.smartbrush_backend.controller;


import com.smartbrush.smartbrush_backend.storage.S3Uploader;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/image/upload")
public class ImageUploadController {

    private final S3Uploader s3Uploader;

    /**
     * ESP32 전용: application/octet-stream 방식
     */
    @Hidden
    @PostMapping(value = "/esp32", consumes = "application/octet-stream")
    public ResponseEntity<?> uploadFromEsp32(@RequestBody byte[] imageData) {
        String fileName = UUID.randomUUID() + ".jpg";
        String imageUrl = s3Uploader.upload(imageData, fileName);
        return ResponseEntity.ok(imageUrl);
    }

    /**
     * Swagger / 웹 프론트 전용: multipart/form-data 방식
     */
    @Operation(summary = "웹에서 이미지 업로드", description = "프론트나 Swagger에서 파일을 업로드할 때 사용")
    @PostMapping(value = "/web", consumes = "multipart/form-data")
    public ResponseEntity<?> uploadFromWeb(@RequestPart MultipartFile file) {
        try {
            String fileName = UUID.randomUUID() + ".jpg";
            String imageUrl = s3Uploader.upload(file.getBytes(), fileName);
            return ResponseEntity.ok(imageUrl);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("이미지 업로드 실패: " + e.getMessage());
        }
    }
}

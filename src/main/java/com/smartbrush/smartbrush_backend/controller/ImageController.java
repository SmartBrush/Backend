package com.smartbrush.smartbrush_backend.controller;

import com.smartbrush.smartbrush_backend.dto.uv.UvRequestDto;
import com.smartbrush.smartbrush_backend.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/api")
public class ImageController {

    @Autowired
    private FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadImage(@RequestBody UvRequestDto dto) {
        try {
            String base64Image = dto.getImage();

            // 공백 및 줄바꿈 제거
            base64Image = base64Image.replaceAll("\\s", "");

            // 디코딩 및 저장
            File savedFile = fileService.saveImage(base64Image);

            return ResponseEntity.ok("이미지 업로드 성공: " + savedFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("이미지 업로드 실패: " + e.getMessage());
        }
    }
}

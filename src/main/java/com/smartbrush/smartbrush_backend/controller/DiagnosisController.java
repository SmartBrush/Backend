package com.smartbrush.smartbrush_backend.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/diagnosis")
public class DiagnosisController {

    @Operation(summary = "두피 이미지 진단 요청", description = "이미지를 업로드하면 진단 결과를 반환합니다.")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> diagnose(
            @Parameter(description = "업로드할 이미지 파일", required = true)
            @RequestPart("image") MultipartFile image) {

        try {
            // Flask 서버로 요청
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("image", new MultipartInputStreamFileResource(image.getInputStream(), image.getOriginalFilename()));

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<String> response = restTemplate.postForEntity(
                    "http://localhost:5000/ai", requestEntity, String.class);

            ObjectMapper objectMapper = new ObjectMapper();
            TypeReference<Map<String, Object>> typeRef = new TypeReference<>() {};
            Map<String, Object> parsed = objectMapper.readValue(response.getBody(), typeRef);
            return ResponseEntity.ok(parsed);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("진단 실패: " + e.getMessage());
        }
    }
}


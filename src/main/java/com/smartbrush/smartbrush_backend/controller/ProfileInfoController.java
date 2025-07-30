package com.smartbrush.smartbrush_backend.controller;

import com.smartbrush.smartbrush_backend.dto.question.ProfileInfoRequestDTO;
import com.smartbrush.smartbrush_backend.dto.question.UserProfileInfoResponseDTO;
import com.smartbrush.smartbrush_backend.service.ProfileInfoService;
import com.smartbrush.smartbrush_backend.service.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/question")
@RequiredArgsConstructor
@Tag(name = "질문 응답", description = "사용자별 질문 API")
public class ProfileInfoController {

    private final ProfileInfoService profileInfoService;

    @PostMapping("/save")
    @Operation(summary = "질문 저장", description = "사용자별 질문의 답변을 저장합니다.")
    public ResponseEntity<UserProfileInfoResponseDTO> saveProfileInfo(
            @RequestBody ProfileInfoRequestDTO request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        var savedInfo = profileInfoService.save(userDetails.getAuthEntity(), request);
        return ResponseEntity.ok(savedInfo);
    }

    @GetMapping
    @Operation(summary = "질문 답변 조회", description = "사용자별 질문 답변 조회합니다.")
    public ResponseEntity<UserProfileInfoResponseDTO> getProfileInfo(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        var result = profileInfoService.getProfileInfo(userDetails.getAuthEntity());
        return ResponseEntity.ok(result);
    }

}
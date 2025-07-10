package com.smartbrush.smartbrush_backend.controller;

import com.smartbrush.smartbrush_backend.dto.uv.UvRequestDto;
import com.smartbrush.smartbrush_backend.dto.uv.UvResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/uv")
public class UvController {

    private UvResult latestResult = null;

    // 데이터 수신
    @PostMapping
    public ResponseEntity<String> receiveUV(@RequestBody UvRequestDto dto) {
        latestResult = new UvResult(dto.getUv(), dto.getState(), dto.getDeviceId());
        System.out.println("[수신] UV: " + dto.getUv() + " | 상태: " + dto.getState());
        return ResponseEntity.ok("UV 데이터 저장 완료");
    }

    // 실시간 조회
    @GetMapping("/latest")
    public ResponseEntity<UvResult> getLatestUV() {
        if (latestResult == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(latestResult);
    }
}

    // 로그인한 사용자의 UV 데이터 조회
//    @GetMapping("/my")
//    public ResponseEntity<List<UvResult>> getMyUVs(HttpServletRequest request) {
//        Long userId = (Long) request.getAttribute("userId");
//
//        if (userId == null) {
//            return ResponseEntity.status(401).build();
//        }
//
//        AuthEntity user = authRepository.findById(userId).orElseThrow();
//        List<UvResult> result = uvResultRepository.findByUserOrderByTimestampDesc(user);
//
//        return ResponseEntity.ok(result);
//    }
//    @GetMapping("/my")
//    public ResponseEntity<?> getMyUvData(HttpServletRequest request) {
//        Long userId = (Long) request.getAttribute("userId");
//        if (userId == null) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
//        }
//
//        List<UvResult> results = uvResultRepository.findByUserId(userId);
//        return ResponseEntity.ok(results);
//    }
//}

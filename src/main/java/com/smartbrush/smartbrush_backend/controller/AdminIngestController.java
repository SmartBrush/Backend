package com.smartbrush.smartbrush_backend.controller;

import com.smartbrush.smartbrush_backend.service.ProductIngestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// 백엔드 적재용
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/ingest")
@Tag(name = "두피 제품 적재", description = "백엔드가 사용할 API 입니다.")
public class AdminIngestController {

    private final ProductIngestService ingestService;

    @Operation(summary = "단일 카테고리 적재", description = "해당 카테고리 데이터 삭제 후 limit개 새로 저장")
    @PostMapping("/{category}")
    public Map<String, Object> ingestOne(@PathVariable String category,
                                         @RequestParam(defaultValue = "150") int limit) throws Exception {
        int saved = ingestService.ingestCategory(category, limit);
        return Map.of("category", category, "saved", saved);
    }

    @Operation(summary = "전체 카테고리 일괄 적재", description = "모든 카테고리 삭제 후 각 limitEach개 저장")
    @PostMapping("/all")
    public Map<String, Object> ingestAll(@RequestParam(defaultValue = "150") int limitEach) throws Exception {
        int total = ingestService.ingestAll(limitEach);
        return Map.of("limitEach", limitEach, "totalSaved", total);
    }
}

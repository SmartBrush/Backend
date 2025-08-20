package com.smartbrush.smartbrush_backend.controller;

import com.smartbrush.smartbrush_backend.service.MagazineIngestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/magazines")
@Tag(name = "관리자 칼럼 적재", description = "백엔드 전용 API 입니다.")
public class AdminMagazineIngestController {

    private final MagazineIngestService magazineIngestService;

    @Operation(summary = "전체 적재", description = "DB 전체 삭제 후 적재")
    @PostMapping("/ingest")
    public Map<String, Object> ingestAll(@RequestParam(defaultValue = "150") int limit) throws Exception {
        int saved = magazineIngestService.ingestAll(limit);
        return Map.of("saved", saved, "limit", limit);
    }
}

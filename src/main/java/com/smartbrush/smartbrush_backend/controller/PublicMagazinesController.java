package com.smartbrush.smartbrush_backend.controller;
import com.smartbrush.smartbrush_backend.entity.Magazine;
import com.smartbrush.smartbrush_backend.repository.MagazineRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/magazines")
@Tag(name = "칼럼 리스트", description = "보그 칼럼 리스트 입니다.")
public class PublicMagazinesController {

    private final MagazineRepository magazineRepository;

    @Operation(summary = "전체 칼럼", description = "size로 개수 제한 (기본 20)")
    @Transactional(readOnly = true)
    @GetMapping("/all")
    public List<Magazine> getAll(@RequestParam(defaultValue = "20") int size) {
        int safe = Math.max(1, size);
        Pageable pageable = PageRequest.of(0, safe);
        return magazineRepository.findAll(pageable).getContent();
    }

    @Operation(summary = "칼럼 10개 추천", description = "칼럼 10개를 랜덤으로 추천합니다.")
    @Transactional(readOnly = true)
    @GetMapping("/recommend")
    public List<Magazine> getRecommend() {
        return magazineRepository.getRecommend();
    }

    @Operation(summary = "칼럼 검색", description = "title을 기준으로 칼럼 검색")
    @Transactional(readOnly = true)
    @GetMapping("/search")
    public List<Magazine> searchMagazines(@RequestParam String keyword,
                                          @RequestParam(defaultValue = "20") int size) {
        int safe = Math.max(1, size);
        Pageable pageable = PageRequest.of(0, safe);
        return magazineRepository.findByTitleContainingIgnoreCase(keyword, pageable).getContent();
    }
}

package com.smartbrush.smartbrush_backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartbrush.smartbrush_backend.entity.Product;
import com.smartbrush.smartbrush_backend.repository.ProductRepository;
import com.smartbrush.smartbrush_backend.service.ProductIngestService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// 백엔드 적재용
@Service
@RequiredArgsConstructor
public class ProductIngestServiceImpl implements ProductIngestService {

    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper;

    private String fileOf(String category) {
        return switch (category) {
            case "shampoo"     -> "products_shampoo.json";
            case "conditioner" -> "products_conditioner.json";
            case "treatment"   -> "products_treatment.json";
            case "essence"     -> "products_essence.json";
            case "tonic"       -> "products_tonic.json";
            default -> throw new IllegalArgumentException("Unknown category: " + category);
        };
    }

    @Override
    @Transactional
    public int ingestCategory(String category, int limit) throws Exception {
        // 1) 자식 삭제 - wishlist
        productRepository.deleteWishlistByCategory(category);

        // 2) 기존 카테고리 데이터 삭제
        productRepository.deleteByCategory(category);

        // 3) classpath에서 JSON 로드 (JAR 환경 포함)
        ClassPathResource resource = new ClassPathResource(fileOf(category));
        try (InputStream in = resource.getInputStream()) {
            List<Map<String, String>> raw = objectMapper.readValue(in, new TypeReference<>() {});
            // 4) 최대 limit개 변환 후 저장
            int safeLimit = Math.max(1, limit);
            List<Product> items = raw.stream()
                    .limit(safeLimit)
                    .map(m -> Product.builder()
                            .brand(m.get("brand"))
                            .name(m.get("name"))
                            .price(m.get("price"))
                            .image(m.get("image"))
                            .link(m.get("link"))
                            .category(category)
                            .build())
                    .collect(Collectors.toList());

            productRepository.saveAll(items);
            return items.size();
        }
    }

    @Override
    @Transactional
    public int ingestAll(int limitEach) throws Exception {
        int total = 0;
        for (String c : List.of("shampoo","conditioner","treatment","essence","tonic")) {
            total += ingestCategory(c, limitEach);
        }
        return total;
    }
}

package com.smartbrush.smartbrush_backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.smartbrush.smartbrush_backend.entity.Magazine;
import com.smartbrush.smartbrush_backend.repository.MagazineRepository;
import com.smartbrush.smartbrush_backend.service.MagazineIngestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MagazineIngestServiceImpl implements MagazineIngestService {

    private final MagazineRepository magazineRepository;
    private final ObjectMapper objectMapper;

    private static final String MAGAZINE_JSON_PATH = "vogue_talmo_articles.json";

    @Override
    @Transactional
    public int ingestAll(int limit) throws Exception {
        int safe = Math.max(1, limit);

        List<Magazine> items = readFromJsonAndMap(safe);

        // 모두 삭제 → 새로 저장
        magazineRepository.deleteAll();
        magazineRepository.saveAll(items);

        log.info("칼럼 DB 적재: {} 개", items.size());
        return items.size();
    }

    private List<Magazine> readFromJsonAndMap(int limit) throws Exception {
        ClassPathResource resource = new ClassPathResource(MAGAZINE_JSON_PATH);
        if (!resource.exists()) {
            throw new IllegalStateException("리소스를 찾을 수 없습니다: " + MAGAZINE_JSON_PATH);
        }

        List<ObjectNode> raw;
        try (InputStream in = resource.getInputStream()) {
            raw = objectMapper.readValue(in, new TypeReference<List<ObjectNode>>() {});
        }
        if (raw == null) raw = List.of();

        List<Magazine> result = new ArrayList<>();
        for (ObjectNode n : raw) {
            String title    = text(n, "title");
            String category = text(n, "category");
            String date     = text(n, "date");
            String writer   = text(n, "writer");
            String image    = text(n, "image");
            String link     = text(n, "link");

            result.add(Magazine.builder()
                    .title(title)
                    .category(category)
                    .date(date)
                    .writer(writer)
                    .image(image)
                    .link(link)
                    .build());

            if (result.size() >= limit) break;
        }
        return result;
    }

    private String text(ObjectNode n, String field) {
        return n.hasNonNull(field) ? n.get(field).asText() : null;
    }
}

package com.smartbrush.smartbrush_backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartbrush.smartbrush_backend.entity.DiagnosisEntity;
import com.smartbrush.smartbrush_backend.entity.Product;
import com.smartbrush.smartbrush_backend.repository.DiagnosisRepository;
import com.smartbrush.smartbrush_backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductRecommendationServiceImpl {

    private final DiagnosisRepository diagnosisRepository;
    private final ProductRepository productRepository;
    private final ObjectMapper om = new ObjectMapper();

    // MBTI → 포함/제외 키워드
    private static final Map<String, List<String>> INCLUDE = Map.of(
            "건조 트러블형", List.of(
                    "저자극","약산성","진정","판테놀","세라마이드","손상","복구",
                    "센텔라","병풀","시카","마데카","알란토인","히알루론","글리세린",
                    "보습","수분","모이스처","리페어","데미지","단백질","앰플","케라틴"
            ),
            "트러블 폭풍형", List.of(
                    "살리실산","티트리","피지","각질케어","징크",
                    "bha","살리실","스케일링","스케일러","디그리징","딥클렌징","각질"
            ),
            "민감 건조형", List.of(
                    "저자극","무향","약산성","센서티브","수분",
                    "fragrance free","무향료","하이포알러","순한","마일드","ph","pH","5.5"
            ),
            "지성 민감형", List.of(
                    "피지","진정","티트리","모공","과다유분","지성",
                    "노세범","세범","쿨링","멘솔","민트","디그리징","피지컷","로즈마리","씨위드","네틀"
            ),
            "건조 비듬형", List.of(
                    "비듬","각질","수분","진정",
                    "anti-dandruff","안티-댄드러프","콜츠푸트","피오니","건성 비듬","보습 비듬"
            ),
            "지성 비듬형", List.of(
                    "비듬","각질","지성","살리실산",
                    "anti-dandruff","zpt","피리치온","징크","씨위드","네틀","노세범","디그리징","딥클렌징"
            ),
            "깔끔 지성형", List.of(
                    "지성","모공","쿨링","산뜻","과다유분","피지",
                    "노세범","세범","딥클렌징","디그리징","프레시","민트","멘솔","쿨","시원","로즈마리","티트리"
            ),
            "밸런스형", List.of(
                    "두피케어","데일리","마일드",
                    "약산성","순한","데일리케어","올인원","라이트","ph","pH","5.5"
            )
    );

    private static final Map<String, List<String>> EXCLUDE = Map.of(
            "건조 트러블형", List.of("강력세정","강한쿨링","극강쿨링","아이스쿨","쿨샷","멘솔폭발"),
            "트러블 폭풍형", List.of("고보습오일","실리콘","오일리치","오일 코팅","아르간오일","코코넛오일","호호바오일"),
            "민감 건조형",   List.of("강한향","강력세정","진한향","퍼퓸 강한"),
            "지성 민감형",   List.of("고보습오일","오일리치","오일 코팅","아르간오일","코코넛오일","호호바오일"),
            "건조 비듬형",   List.of("강력세정","강한쿨링","아이스쿨","쿨샷"),
            "지성 비듬형",   List.of("고보습오일","오일리치","오일 코팅"),
            "깔끔 지성형",   List.of("고보습오일","오일리치","오일 코팅"),
            "밸런스형",     List.of()
    );

    public List<Product> recommendByCategory(String email, String category, int size) {
        // 최근 진단 (오늘 없으면 최신 1건)
        DiagnosisEntity dx = diagnosisRepository.findByEmailAndDiagnosedDate(email, LocalDate.now())
                .orElseGet(() -> diagnosisRepository.findTopByEmailOrderByDiagnosedDateDesc(email).orElse(null));
        if (dx == null) return List.of();

        Map<String, Object> result;
        try {
            result = om.readValue(dx.getResultJson(), Map.class);
        } catch (Exception e) {
            return List.of();
        }

        String mbti = (String) result.getOrDefault("scalpMbti", "밸런스형");
        List<String> include = INCLUDE.getOrDefault(mbti, INCLUDE.get("밸런스형"));
        List<String> exclude = EXCLUDE.getOrDefault(mbti, List.of());

        // 후보 넉넉히 조회 후 토큰기반 스코어 → 정렬
        var candidates = productRepository.findByCategory(category, PageRequest.of(0, 150)).getContent();

        return candidates.stream()
                .filter(p -> !containsAnyToken(p.getName(), exclude))
                .sorted(Comparator.comparingInt(
                        (Product p) -> scoreByTokens(p.getName(), include, exclude)
                ).reversed())
                .limit(Math.max(size, 1))
                .collect(Collectors.toList());
    }

    // ====== 이름 토큰화/매칭 유틸 ======

    private static int scoreByTokens(String productName, List<String> include, List<String> exclude) {
        Set<String> tokens = tokenize(productName);
        int sc = 0;
        for (String inc : include) {
            String key = normalize(inc.toLowerCase());
            if (tokens.contains(key)) sc += 10;
        }
        for (String exc : exclude) {
            String key = normalize(exc.toLowerCase());
            if (tokens.contains(key)) sc -= 15;
        }
        return sc;
    }

    private static boolean containsAnyToken(String name, List<String> words) {
        Set<String> tokens = tokenize(name);
        for (String w : words) {
            if (tokens.contains(normalize(w.toLowerCase()))) return true;
        }
        return false;
    }

    private static Set<String> tokenize(String name) {
        if (name == null) return Set.of();
        String s = name
                .replaceAll("[\\[\\]{}]", " ")
                .replaceAll("[()]", " ")
                .replaceAll("[^가-힣a-zA-Z0-9/\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim()
                .toLowerCase();

        List<String> tokens = new ArrayList<>();
        for (String tok : s.split(" ")) {
            if (tok.contains("/")) tokens.addAll(Arrays.asList(tok.split("/")));
            else tokens.add(tok);
        }
        return tokens.stream()
                .map(ProductRecommendationServiceImpl::normalize)
                .filter(t -> t.length() >= 2)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static String normalize(String t) {
        // 단위 표기 정리
        t = t.replaceAll("(ml|g|mg|kg|l)$", "");
        return t;
    }
}

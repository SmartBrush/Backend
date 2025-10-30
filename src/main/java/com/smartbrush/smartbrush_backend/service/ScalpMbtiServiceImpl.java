package com.smartbrush.smartbrush_backend.service;

import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class ScalpMbtiServiceImpl {

    public String getMbti(int sensitivity, int sebum, int scaling, int density, int thickness) {

        boolean lowDensity    = density   <= 45;
        boolean lowThickness  = thickness <= 45;
        boolean weakStructure = lowDensity || lowThickness;
        boolean strongStructure = density >= 65 && thickness >= 65;

        // 1) 트러블 폭풍형
        if ((sensitivity >= 60 && scaling >= 60 && sebum >= 65 && weakStructure)
                || (sensitivity >= 60 && scaling >= 60 && sebum >= 70)
                || (sensitivity >= 70 && scaling >= 70 && sebum >= 60)) {
            return "트러블 폭풍형";
        }

        // 2) 지성 민감형
        if ((sensitivity >= 70 && sebum >= 70)
                || (sensitivity >= 65 && sebum >= 65 && weakStructure)) {
            return "지성 민감형";
        }

        // 3) 민감 건조형
        if ((sensitivity >= 70 && sebum <= 40)
                || (sensitivity >= 65 && sebum <= 45 && weakStructure)) {
            return "민감 건조형";
        }

        // 4) 지성 비듬형
        if ((scaling >= 70 && sebum >= 70)
                || (scaling >= 65 && sebum >= 65 && weakStructure)) {
            return "지성 비듬형";
        }

        // 5) 건조 비듬형
        if ((scaling >= 70 && sebum <= 40)
                || (scaling >= 65 && sebum <= 45 && weakStructure)) {
            return "건조 비듬형";
        }

        // 6) 건조 트러블형
        if ((sensitivity >= 60 && scaling >= 60 && sebum <= 45)
                || (sensitivity >= 55 && scaling >= 55 && sebum <= 50 && weakStructure)) {
            return "건조 트러블형";
        }

        // 7) 깔끔 지성형
        if (sebum >= 65 && sensitivity <= 45 && scaling <= 45 && strongStructure) {
            return "깔끔 지성형";
        }
        // 구조가 약해도 유분이 높고 나머지가 낮으면 fallback으로 인정
        if (sebum >= 70 && sensitivity <= 40 && scaling <= 40) {
            return "깔끔 지성형";
        }

        // 8) 약한 구조로 인한 보정
        if (weakStructure && sensitivity >= 55 && scaling >= 55) {
            return (sebum <= 55) ? "건조 트러블형" : "트러블 폭풍형";
        }

        // 9) 밸런스형
        boolean midSensitivity = sensitivity >= 40 && sensitivity <= 60;
        boolean midSebum       = sebum       >= 40 && sebum       <= 60;
        boolean midScaling     = scaling     >= 40 && scaling     <= 60;
        if (midSensitivity && midSebum && midScaling && strongStructure) {
            return "밸런스형";
        }

        // 10) 그 외 애매하면 기본값
        return "밸런스형";
    }

    /** 새 방식: confidence까지 사용 */
    public String getMbtiWithConfidence(
            Map<String, Map<String, Object>> parsed,
            int fallbackSensitivity, int fallbackSebum, int fallbackScaling, int fallbackDensity, int fallbackThickness
    ) {
        // 1) 연속 점수 산출 (MBTI 판단에만 사용; 저장되는 값은 건드리지 않음)
        int sens = avg(
                cont(parsed, "모낭사이홍반", false),
                cont(parsed, "모낭홍반농포", false)
        );
        int scal = avg(
                cont(parsed, "미세각질", false),
                cont(parsed, "비듬", false)
        );
        int sebum = cont(parsed, "피지과다", false);
        int density = cont(parsed, "탈모", true);       // 구조 항목은 inverted
        int thickness = cont(parsed, "모발밀도", true);

        // fallback: 혹시 키 누락/파싱 실패 시 기존 점수 사용
        if (sens == -1) sens = fallbackSensitivity;
        if (scal == -1) scal = fallbackScaling;
        if (sebum == -1) sebum = fallbackSebum;
        if (density == -1) density = fallbackDensity;
        if (thickness == -1) thickness = fallbackThickness;

        // 2) 특징 축 구성 (0~100)
        double irr = (sens + scal) / 2.0;                 // 자극/각질
        double oil = sebum;                               // 유분
        double struct = (density + thickness) / 2.0;      // 구조(높을수록 좋음)

        // 3) 정규화(0~1) + 구조 약함 캡
        double I = irr / 100.0;
        double O = oil / 100.0;
        double S = struct / 100.0;
        double W = Math.min(1.0 - S, 0.6);                // 구조 약함의 과도한 영향 제한
        double midI = 1.0 - Math.abs(I - 0.5);
        double midO = 1.0 - Math.abs(O - 0.5);

        // 4) 유형별 점수 (가중치는 초기값, 운영하며 조정 가능)
        Map<String, Double> score = new LinkedHashMap<>();
        score.put("트러블 폭풍형",   0.40*I + 0.25*O + 0.25*W + 0.10*midO);
        score.put("지성 민감형",     0.45*I + 0.40*O + 0.10*(1.0-W) + 0.05*midO);
        score.put("민감 건조형",     0.50*I + 0.05*O + 0.35*W + 0.10*midI);
        score.put("지성 비듬형",     0.30*I + 0.55*O + 0.10*(1.0-W) + 0.05*midI);
        score.put("건조 비듬형",     0.35*I + 0.10*O + 0.45*W + 0.10*midI);
        // 구조 의존 과도 ↓, 자극 비중 ↑ (몰림 방지)
        score.put("건조 트러블형",   0.45*I + 0.10*O + 0.30*W + 0.15*midI);
        // 구조가 너무 약하면 보너스 감소
        score.put("깔끔 지성형",     -0.10*I + 0.65*O + 0.35*(W >= 0.4 ? 0.0 : S));
        score.put("밸런스형",         0.30*midI + 0.30*midO + 0.40*S - 0.10*W);

        // 5) argmax 선택 (미세 잡음으로 동점 방지)
        String best = null;
        double bestVal = -1e9;
        for (var e : score.entrySet()) {
            double v = e.getValue() + 1e-6 * e.getKey().hashCode();
            if (v > bestVal) { bestVal = v; best = e.getKey(); }
        }
        return best;
    }

    // ===== 내부 유틸 =====

    /** parsed에서 class_index+confidence를 연속 점수(0~100)로 변환. inverted면 반전. 실패 시 -1 */
    private int cont(Map<String, Map<String, Object>> parsed, String key, boolean inverted) {
        try {
            Map<String,Object> m = parsed.get(key);
            if (m == null) return -1;
            Integer cls = (Integer) m.get("class_index");
            Double conf = null;
            Object cObj = m.get("confidence");
            if (cObj instanceof Number n) conf = n.doubleValue();
            return continuousScore(cls, conf, inverted);
        } catch (Exception e) {
            return -1;
        }
    }

    /** 연속 스코어링: class_index 구간 + confidence로 보간 */
    private int continuousScore(Integer cls, Double conf, boolean inverted) {
        if (cls == null) return 55;
        double c = (conf == null) ? 0.5 : Math.max(0, Math.min(1, conf));

        double lo, hi;
        switch (cls) {
            case 0 -> { lo = 20; hi = 40; }      // 양호
            case 1, 2 -> { lo = 45; hi = 60; }   // 보통
            case 3 -> { lo = 70; hi = 90; }      // 심각
            default -> { lo = 45; hi = 60; }
        }
        // conf 높을수록 hi쪽
        double v = lo + (c * (hi - lo));
        if (inverted) v = 100 - v;
        return (int)Math.round(v);
    }

    private int avg(int a, int b) {
        return (a < 0 || b < 0) ? Math.max(a, b) : Math.round((a + b) / 2f);
    }
}

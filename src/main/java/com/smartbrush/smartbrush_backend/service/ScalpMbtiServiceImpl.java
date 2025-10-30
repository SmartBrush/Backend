package com.smartbrush.smartbrush_backend.service;

import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class ScalpMbtiServiceImpl {

    /**
     * 연속 스코어(0~100) + 게이트 + 가중치 기반 MBTI 산출
     */
    public String getMbtiWithConfidence(
            Map<String, Map<String, Object>> parsed,
            int fallbackSensitivity, int fallbackSebum, int fallbackScaling, int fallbackDensity, int fallbackThickness
    ) {
        // 1) 연속 점수 산출 (MBTI 판단 전용)
        int sens = avg(
                cont(parsed, "모낭사이홍반", false),
                cont(parsed, "모낭홍반농포", false)
        );
        int scal = avg(
                cont(parsed, "미세각질", false),
                cont(parsed, "비듬", false)
        );
        int sebum     = cont(parsed, "피지과다", false);
        int density   = cont(parsed, "탈모", true);       // 구조 항목은 inverted
        int thickness = cont(parsed, "모발밀도", true);

        // 2) 폴백
        if (sens == -1) sens = fallbackSensitivity;
        if (scal == -1) scal = fallbackScaling;
        if (sebum == -1) sebum = fallbackSebum;
        if (density == -1) density = fallbackDensity;
        if (thickness == -1) thickness = fallbackThickness;

        // 3) 특징 축 (0~100 → 0~1)
        double irr    = (sens + scal) / 2.0;          // 자극/각질(높을수록 안좋음)
        double oil    = sebum;                         // 유분(높을수록 지성)
        double struct = (density + thickness) / 2.0;   // 구조(높을수록 좋음)

        double I = irr / 100.0;
        double O = oil / 100.0;
        double S = struct / 100.0;

        // 구조 약함의 영향 상한 완화 (0.6 → 0.5)
        double W = Math.min(1.0 - S, 0.5);

        double midI = 1.0 - Math.abs(I - 0.5);
        double midO = 1.0 - Math.abs(O - 0.5);

        // 4) 유형별 게이트 (밸런스형 강화)
        boolean gateStorm    = (I >= 0.60) && ((O >= 0.60) || (W >= 0.40));
        boolean gateOilySens = (I >= 0.60) && (O >= 0.60);
        boolean gateSensDry  = (I >= 0.60) && (O <= 0.45) && (W >= 0.30);
        boolean gateOilyD    = (O >= 0.60) && (I >= 0.50);
        boolean gateDryD     = (O <= 0.45) && (I >= 0.55) && (W >= 0.30);
        boolean gateDryT     = (I >= 0.55) && (O <= 0.55) && (W >= 0.35);
        boolean gateCleanO   = (O >= 0.65) && (I <= 0.45) && (W <= 0.25) && (S >= 0.55);
        // 밸런스형: 중간값 근접성을 더 엄격하게, 구조도 더 좋아야 통과
        boolean gateBalance  = (Math.abs(I - 0.5) <= 0.08) && (Math.abs(O - 0.5) <= 0.08) && (S >= 0.65);

        // 5) 유형별 원점수 (S 가중치 완화)
        double storm    = 0.45*I + 0.25*O + 0.25*W + 0.05*midO;             // 트러블 폭풍형
        double oilySens = 0.45*I + 0.40*O + 0.10*(1.0 - W) + 0.05*midO;     // 지성 민감형
        double sensDry  = 0.52*I + 0.06*O + 0.32*W + 0.10*midI;             // 민감 건조형
        double oilyD    = 0.30*I + 0.58*O + 0.07*(1.0 - W) + 0.05*midI;     // 지성 비듬형
        double dryD     = 0.38*I + 0.10*O + 0.42*W + 0.10*midI;             // 건조 비듬형
        double dryT     = 0.48*I + 0.10*O + 0.32*W + 0.10*midI;             // 건조 트러블형
        double cleanO   = -0.12*I + 0.68*O + (W >= 0.40 ? 0.0 : 0.32*S);    // 깔끔 지성형
        // 밸런스형 구조 보너스 0.52 → 0.40
        double balance  = 0.24*midI + 0.24*midO + 0.40*S - 0.22*(Math.abs(I - 0.5) + Math.abs(O - 0.5));

        // 6) 게이트 미충족 시 강등
        if (!gateStorm)    storm    -= 1.0;
        if (!gateOilySens) oilySens -= 1.0;
        if (!gateSensDry)  sensDry  -= 1.0;
        if (!gateOilyD)    oilyD    -= 1.0;
        if (!gateDryD)     dryD     -= 1.0;
        if (!gateDryT)     dryT     -= 1.0;
        if (!gateCleanO)   cleanO   -= 1.0;
        if (!gateBalance)  balance  -= 1.0;

        Map<String, Double> score = new LinkedHashMap<>();
        score.put("트러블 폭풍형",   storm);
        score.put("지성 민감형",     oilySens);
        score.put("민감 건조형",     sensDry);
        score.put("지성 비듬형",     oilyD);
        score.put("건조 비듬형",     dryD);
        score.put("건조 트러블형",   dryT);
        score.put("깔끔 지성형",     cleanO);
        score.put("밸런스형",         balance);

        // 디버깅 로그 (필요 시 잠깐 활성화)
        System.out.printf(
                "[MBTI DEBUG] I=%.2f O=%.2f S=%.2f W=%.2f midI=%.2f midO=%.2f | " +
                        "gate(storm=%s, oilySens=%s, sensDry=%s, oilyD=%s, dryD=%s, dryT=%s, cleanO=%s, balance=%s) | " +
                        "score(storm=%.3f, oilySens=%.3f, sensDry=%.3f, oilyD=%.3f, dryD=%.3f, dryT=%.3f, cleanO=%.3f, balance=%.3f)%n",
                I, O, S, W, midI, midO,
                gateStorm, gateOilySens, gateSensDry, gateOilyD, gateDryD, gateDryT, gateCleanO, gateBalance,
                storm, oilySens, sensDry, oilyD, dryD, dryT, cleanO, balance
        );

        // 7) argmax
        String best = null;
        double bestVal = -1e9;
        double eps = 1e-6;
        for (var e : score.entrySet()) {
            double v = e.getValue() + eps * e.getKey().hashCode();
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

            // 안전 캐스팅: Integer/Long 구분 없이 Number로 받아서 처리
            Number clsNum = (Number) m.get("class_index");
            Integer cls = (clsNum == null) ? null : clsNum.intValue();

            Double conf = null;
            Object cObj = m.get("confidence");
            if (cObj instanceof Number n) conf = n.doubleValue();

            return continuousScore(cls, conf, inverted);
        } catch (Exception e) {
            return -1;
        }
    }

    /** 연속 스코어링: class_index 구간 + confidence 보간 */
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
        double v = lo + (c * (hi - lo));        // conf 높을수록 hi쪽
        if (inverted) v = 100 - v;
        return (int)Math.round(v);
    }

    /** 두 값 평균(한쪽이 -1이면 다른 값 사용; 둘 다 -1이면 -1) */
    private int avg(int a, int b) {
        if (a < 0 && b < 0) return -1;
        if (a < 0) return b;
        if (b < 0) return a;
        return Math.round((a + b) / 2f);
    }
}

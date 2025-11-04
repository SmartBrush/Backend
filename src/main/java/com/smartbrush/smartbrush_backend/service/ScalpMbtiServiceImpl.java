package com.smartbrush.smartbrush_backend.service;

import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class ScalpMbtiServiceImpl {

    public String getMbti(int sensitivity, int sebum, int scaling, int density, int thickness) {

        boolean lowDensity = density <= 45;
        boolean lowThickness = thickness <= 45;
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
        boolean midSebum = sebum >= 40 && sebum <= 60;
        boolean midScaling = scaling >= 40 && scaling <= 60;
        if (midSensitivity && midSebum && midScaling && strongStructure) {
            return "밸런스형";
        }

        // 10) 그 외 애매하면 기본값
        return "밸런스형";
    }
}
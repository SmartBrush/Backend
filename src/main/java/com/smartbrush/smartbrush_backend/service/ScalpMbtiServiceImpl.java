package com.smartbrush.smartbrush_backend.service;

import org.springframework.stereotype.Service;

@Service
public class ScalpMbtiServiceImpl {

    public String getMbti(Integer sensIdx, Integer sebumIdx, Integer scalingIdx) {

        // 값이 없으면 기본값 1
        if (sensIdx == null) sensIdx = 1;
        if (sebumIdx == null) sebumIdx = 1;
        if (scalingIdx == null) scalingIdx = 1;

        // 1. 밸런스형
        if (sensIdx == 0 && sebumIdx == 1 && scalingIdx == 0) {
            return "밸런스형";
        }

        // 2. 지성 그룹
        if (sebumIdx == 2 || sebumIdx == 3) {
            // 트러블형: 민감도 2~3 + 각질 2~3
            if ((sensIdx == 2 || sensIdx == 3) && (scalingIdx == 2 || scalingIdx == 3)) {
                return "지성 트러블형";
            }

            // 민감형: 민감도 2~3 + 각질 0~1
            if ((sensIdx == 2 || sensIdx == 3) && (scalingIdx == 0 || scalingIdx == 1)) {
                return "지성 민감형";
            }

            // 비듬형: 민감도 0~1 + 각질 2~3
            if ((sensIdx == 0 || sensIdx == 1) && (scalingIdx == 2 || scalingIdx == 3)) {
                return "지성 비듬형";
            }

            // 깔끔형: 민감도 0~1 + 각질 0~1
            if ((sensIdx == 0 || sensIdx == 1) && (scalingIdx == 0 || scalingIdx == 1)) {
                return "지성 깔끔형";
            }
        }

        // 3. 건성 그룹
        if (sebumIdx == 0 || sebumIdx == 1) {
            // 트러블형: 민감도 2~3 + 각질 2~3
            if ((sensIdx == 2 || sensIdx == 3) && (scalingIdx == 2 || scalingIdx == 3)) {
                return "건성 트러블형";
            }

            // 민감형: 민감도 2~3 + 각질 0~1
            if ((sensIdx == 2 || sensIdx == 3) && (scalingIdx == 0 || scalingIdx == 1)) {
                return "건성 민감형";
            }

            // 비듬형: 민감도 0~1 + 각질 2~3
            if ((sensIdx == 0 || sensIdx == 1) && (scalingIdx == 2 || scalingIdx == 3)) {
                return "건성 비듬형";
            }

            // 깔끔형: 민감도 0~1 + 각질 0~1
            if ((sensIdx == 0 || sensIdx == 1) && (scalingIdx == 0 || scalingIdx == 1)) {
                return "건성 깔끔형";
            }
        }
        return "밸런스형";
    }
}
package com.smartbrush.smartbrush_backend.service;

import org.springframework.stereotype.Service;

@Service
public class ScalpMbtiServiceImpl {

    public String getMbti(int sensitivity, int sebum, int scaling, int density, int thickness) {
        if (sensitivity >= 70 && sebum >= 70) {
            return "지성 민감형";
        } else if (sensitivity >= 70 && sebum <= 40) {
            return "민감 건조형";
        } else if (scaling >= 70 && sebum >= 70) {
            return "지성 비듬형";
        } else if (scaling >= 70 && sebum <= 40) {
            return "건조 비듬형";
        } else if (sensitivity >= 60 && scaling >= 60 && sebum <= 45) {
            return "건조 트러블형";
        } else if (sensitivity >= 60 && scaling >= 60 && sebum >= 65) {
            return "트러블 폭풍형";
        } else if (sebum >= 65 && sensitivity <= 45 && scaling <= 45) {
            return "깔끔 지성형";
        } else {
            return "밸런스형";
        }
    }
}

package com.smartbrush.smartbrush_backend.service;

import com.smartbrush.smartbrush_backend.entity.DiagnosisImageEntity;
import com.smartbrush.smartbrush_backend.repository.DiagnosisImageRepository;
import lombok.RequiredArgsConstructor;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.List;


@Service
@RequiredArgsConstructor
public class DiagnosisImageService {

    private final DiagnosisImageRepository diagnosisImageRepository;


    public List<DiagnosisImageEntity> selectTop4Images(String email) {
        return diagnosisImageRepository.findTop4ByEmailOrderByCapturedAtDesc(email);
    }

    // 라플라시안 기반 선명도 계산
    private double calculateSharpness(BufferedImage img) {
        Mat mat = bufferedImageToMat(img);
        Mat laplacian = new Mat();
        Imgproc.Laplacian(mat, laplacian, CvType.CV_64F);
        MatOfDouble mean = new MatOfDouble();
        MatOfDouble stddev = new MatOfDouble();
        Core.meanStdDev(laplacian, mean, stddev);
        return stddev.get(0, 0)[0];
    }

    private Mat bufferedImageToMat(BufferedImage bi) {
        byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
        Mat mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC3);
        mat.put(0, 0, data);
        return mat;
    }
}



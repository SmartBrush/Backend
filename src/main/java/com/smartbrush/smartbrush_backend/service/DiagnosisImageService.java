package com.smartbrush.smartbrush_backend.service;

import com.smartbrush.smartbrush_backend.entity.DiagnosisImageEntity;
import com.smartbrush.smartbrush_backend.repository.DiagnosisImageRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

//@Service
//@RequiredArgsConstructor
//public class DiagnosisImageService {
//
//    private final DiagnosisImageRepository diagnosisImageRepository;
//
//    public List<DiagnosisImageEntity> selectTop4Images(String email) {
//        List<DiagnosisImageEntity> images = diagnosisImageRepository
//                .findTop100ByEmailOrderByCapturedAtDesc(email);
//
//        List<Pair<Double, DiagnosisImageEntity>> scored = new ArrayList<>();
//
//        for (DiagnosisImageEntity img : images) {
//            try {
//                BufferedImage buffered = ImageIO.read(new ByteArrayInputStream(img.getImageData()));
//                double score = calculateSharpness(buffered);
//                scored.add(Pair.of(score, img));
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//
//        return scored.stream()
//                .sorted((a, b) -> Double.compare(b.getLeft(), a.getLeft())) // 높은 순
//                .limit(4)
//                .map(Pair::getRight)
//                .toList();
//    }
//
//    // 라플라시안 기반 선명도 계산
//    private double calculateSharpness(BufferedImage img) {
//        Mat mat = bufferedImageToMat(img);
//        Mat laplacian = new Mat();
//        Imgproc.Laplacian(mat, laplacian, CvType.CV_64F);
//        MatOfDouble mean = new MatOfDouble();
//        MatOfDouble stddev = new MatOfDouble();
//        Core.meanStdDev(laplacian, mean, stddev);
//        return stddev.get(0, 0)[0]; // 표준편차 = 선명도
//    }
//
//    private Mat bufferedImageToMat(BufferedImage bi) {
//        byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
//        Mat mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC3);
//        mat.put(0, 0, data);
//        return mat;
//    }
//}


import com.smartbrush.smartbrush_backend.entity.DiagnosisImageEntity;
import com.smartbrush.smartbrush_backend.repository.DiagnosisImageRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DiagnosisImageService {

    private final DiagnosisImageRepository diagnosisImageRepository;

    public List<DiagnosisImageEntity> selectTop4Images(String email) {
        List<DiagnosisImageEntity> images = diagnosisImageRepository
                .findTop100ByEmailOrderByCapturedAtDesc(email);

        List<Pair<Double, DiagnosisImageEntity>> scored = new ArrayList<>();

        for (DiagnosisImageEntity img : images) {
            try {
                // 👉 S3 URL에서 이미지를 읽음
                BufferedImage buffered = ImageIO.read(new URL(img.getImageUrl()));
                double score = calculateSharpness(buffered);
                scored.add(Pair.of(score, img));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return scored.stream()
                .sorted((a, b) -> Double.compare(b.getLeft(), a.getLeft()))
                .limit(4)
                .map(Pair::getRight)
                .toList();
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



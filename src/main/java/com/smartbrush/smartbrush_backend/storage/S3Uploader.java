//package com.smartbrush.smartbrush_backend.storage;
//
//import com.amazonaws.services.s3.AmazonS3;
//import com.amazonaws.services.s3.model.ObjectMetadata;
//import com.amazonaws.services.s3.model.PutObjectRequest;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
//import java.io.ByteArrayInputStream;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class S3Uploader {
//
//    private final AmazonS3 amazonS3;
//
//    @Value("${cloud.aws.s3.bucket}")
//    private String bucket;
//
//    @Value("${cloud.aws.region.static}") // ✅ region 가져오기
//    private String region;
//
//    public String upload(byte[] imageData, String fileName) {
//        try {
//            ObjectMetadata metadata = new ObjectMetadata();
//            metadata.setContentLength(imageData.length);
//            metadata.setContentType("image/jpeg");
//
//            PutObjectRequest request = new PutObjectRequest(
//                    bucket,
//                    fileName,
//                    new ByteArrayInputStream(imageData),
//                    metadata
//            );
//
//            amazonS3.putObject(request);
//
//            String url = "https://s3." + region + ".amazonaws.com/" + bucket + "/" + fileName;
//
//            log.info("✅ S3 업로드 완료: {}", url);
//            return url;
//
//        } catch (Exception e) {
//            log.error("❌ S3 업로드 실패", e);
//            throw new RuntimeException("S3 업로드 실패: " + e.getMessage());
//        }
//    }
//
//}




package com.smartbrush.smartbrush_backend.storage;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Uploader {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region.static}") // ✅ region 가져오기
    private String region;

    public String upload(byte[] imageData, String fileName) {
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(imageData.length);
            metadata.setContentType("image/jpeg");

            PutObjectRequest request = new PutObjectRequest(
                    bucket,
                    fileName,
                    new ByteArrayInputStream(imageData),
                    metadata
            );

            amazonS3.putObject(request);

            String url = "https://s3." + region + ".amazonaws.com/" + bucket + "/" + fileName;
            log.info("✅ S3 업로드 완료: {}", url);
            return url;

        } catch (Exception e) {
            log.error("❌ S3 업로드 실패", e);
            throw new RuntimeException("S3 업로드 실패: " + e.getMessage());
        }
    }

}

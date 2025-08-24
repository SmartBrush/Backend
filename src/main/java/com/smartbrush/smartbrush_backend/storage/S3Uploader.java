package com.smartbrush.smartbrush_backend.storage;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Uploader {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region.static}")
    private String region; // 예: us-east-1

    /** 편의용: 기본 JPEG */
    public String upload(byte[] bytes, String key) {
        return upload(bytes, key, "image/jpeg");
    }

    /** Content-Type 지정 가능한 업로드 */
    public String upload(byte[] bytes, String key, String contentType) {
        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("빈 바이트 배열입니다.");
        }
        if (contentType == null || contentType.isBlank()) {
            contentType = "application/octet-stream";
        }

        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentType(contentType);
        meta.setContentLength(bytes.length);

        try (InputStream in = new ByteArrayInputStream(bytes)) {
            PutObjectRequest req = new PutObjectRequest(bucket, key, in, meta);
            amazonS3.putObject(req);

            // ‘객체 URL(경로형)’로 고정: https://s3.{region}.amazonaws.com/{bucket}/{key}
            String objectUrl = "https://s3." + region + ".amazonaws.com/" + bucket + "/" + key;

            log.info("✅ S3 업로드 성공: {}", objectUrl);
            return objectUrl;
        } catch (Exception e) {
            log.error("❌ S3 업로드 실패 (key: {})", key, e);
            throw new RuntimeException("S3 업로드 실패: " + e.getMessage(), e);
        }
    }
}

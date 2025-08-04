package com.smartbrush.smartbrush_backend.service;

import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;

@Service
public class FileService {

    public File saveImage(String base64Image) throws IOException {
        // MIME 헤더 제거
        if (base64Image.contains(",")) {
            base64Image = base64Image.substring(base64Image.indexOf(",") + 1);
        }

        // 공백 제거
        base64Image = base64Image.replaceAll("\\s+", "");

        byte[] decodedBytes;
        try {
            decodedBytes = Base64.getDecoder().decode(base64Image); // getMimeDecoder는 제거
        } catch (IllegalArgumentException e) {
            throw new IOException("Base64 디코딩 실패", e);
        }

        File file = new File("uploaded_" + System.currentTimeMillis() + ".jpg");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(decodedBytes);
        }

        return file;
    }

}

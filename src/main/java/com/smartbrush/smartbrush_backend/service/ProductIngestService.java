package com.smartbrush.smartbrush_backend.service;

// 백엔드 적재용
public interface ProductIngestService {
    int ingestCategory(String category, int limit) throws Exception;
    int ingestAll(int limitEach) throws Exception;
}

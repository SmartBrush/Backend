package com.smartbrush.smartbrush_backend.repository;

import com.smartbrush.smartbrush_backend.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByCategory(String category, Pageable pageable);
    Page<Product> findByCategoryAndNameContainingIgnoreCase(String category, String keyword, Pageable pageable);

    void deleteByCategory(String category);

    @Modifying
    @Transactional
    @Query(value = """
        DELETE pw
        FROM product_wishlist pw
        JOIN product p ON pw.product_id = p.id
        WHERE p.category = :category
        """, nativeQuery = true)
    void deleteWishlistByCategory(@Param("category") String category);
}

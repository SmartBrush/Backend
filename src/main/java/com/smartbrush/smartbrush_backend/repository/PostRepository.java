package com.smartbrush.smartbrush_backend.repository;

import com.smartbrush.smartbrush_backend.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findAll();
    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.tags")
    List<Post> findAllWithTags();
}

package com.smartbrush.smartbrush_backend.repository;

import com.smartbrush.smartbrush_backend.entity.Comment;
import com.smartbrush.smartbrush_backend.entity.Community;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByCommunityOrderByCreatedAtAsc(Community community);
    List<Comment> findByUserId(Long userId);
}

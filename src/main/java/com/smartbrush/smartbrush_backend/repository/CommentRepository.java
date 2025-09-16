package com.smartbrush.smartbrush_backend.repository;

import com.smartbrush.smartbrush_backend.entity.Comment;
import com.smartbrush.smartbrush_backend.entity.Community;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByCommunityOrderByCreatedAtAsc(Community community);
    List<Comment> findByUserId(Long userId);
    long countByCommunity_Id(Long communityId);

    @Query("select c from Comment c join fetch c.community where c.userId = :userId")
    List<Comment> findByUserIdWithCommunity(@Param("userId") Long userId);
}

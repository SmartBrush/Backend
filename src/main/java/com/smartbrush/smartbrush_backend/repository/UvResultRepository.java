package com.smartbrush.smartbrush_backend.repository;


import com.smartbrush.smartbrush_backend.entity.AuthEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import com.smartbrush.smartbrush_backend.entity.UvResult;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;
import java.util.Optional;
public interface UvResultRepository extends JpaRepository<UvResult, Long> {
    Optional<UvResult> findTopByUserOrderByTimestampDesc(AuthEntity user);

    @Query("SELECT u FROM UvResult u WHERE u.user = :user AND FUNCTION('DATE_FORMAT', u.timestamp, '%Y-%m-%d') = :date")
    Optional<UvResult> findByUserAndDate(@Param("user") AuthEntity user, @Param("date") String date);

    List<UvResult> findAllByUserOrderByTimestampDesc(AuthEntity user);

}


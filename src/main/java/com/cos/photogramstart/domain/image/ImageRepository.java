package com.cos.photogramstart.domain.image;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ImageRepository extends JpaRepository<Image, Integer> {

    @Query("SELECT i FROM Image i JOIN FETCH i.user " +
            "WHERE i.user.id IN (SELECT s.toUser.id FROM Subscribe s WHERE s.fromUser.id = :principalId) " +
            "ORDER BY i.createDate DESC")
    Page<Image> mStory(@Param("principalId") int principalId, Pageable pageable);

    @Query(value = "SELECT i.* FROM image_tb i INNER JOIN (SELECT imageId, COUNT(imageId) likeCount FROM likes_tb GROUP BY imageId) c ON i.id = c.imageId ORDER BY c.likeCount DESC", nativeQuery = true)
    List<Image> mPopular();
}

package com.example.collection_manager.repositories;

import com.example.collection_manager.enums.Visibility;
import com.example.collection_manager.models.Collection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CollectionRepository extends JpaRepository<Collection, Long> {

    @EntityGraph(attributePaths = {"user"})
    List<Collection> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"user"})
    List<Collection> findByUserIdAndVisibility(Long userId, Visibility visibility);

    @EntityGraph(attributePaths = {"user"})
    List<Collection> findByVisibility(Visibility visibility);

    @EntityGraph(attributePaths = {"user"})
    List<Collection> findByUserIdInAndVisibility(List<Long> userIds, Visibility visibility);

    List<Collection> findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(Long userId, LocalDateTime after);

    @Query("""

            SELECT c
    FROM Collection c
    WHERE (
        c.user.id = :userId
        OR c.visibility = com.example.collection_manager.enums.Visibility.PUBLIC
        OR (c.visibility = com.example.collection_manager.enums.Visibility.FRIENDS AND c.user.id IN :friendIds)
    )
    AND coalesce(c.createdAt, c.updatedAt) >= :since
    ORDER BY coalesce(c.createdAt, c.updatedAt) DESC
    """)
    List<Collection> findRecentVisible(
            @Param("userId") Long userId,
            @Param("friendIds") List<Long> friendIds,
            @Param("since") LocalDateTime since,
            Pageable pageable);

    @Query("""
    SELECT COUNT(c)
    FROM Collection c
    WHERE (
        c.user.id = :userId
        OR c.visibility = com.example.collection_manager.enums.Visibility.PUBLIC
        OR (c.visibility = com.example.collection_manager.enums.Visibility.FRIENDS AND c.user.id IN :friendIds)
    )
    AND coalesce(c.createdAt, c.updatedAt) >= :since
    """)
    long countRecentVisible(
            @Param("userId") Long userId,
            @Param("friendIds") List<Long> friendIds,
            @Param("since") LocalDateTime since);
    }

package com.example.collection_manager.repositories;

import com.example.collection_manager.models.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByTagName(String tagName);

    @Query("SELECT t FROM Tag t WHERE LOWER(t.tagName) IN :names")
    List<Tag> findAllByTagNameInIgnoreCase(@Param("names") List<String> names);
}

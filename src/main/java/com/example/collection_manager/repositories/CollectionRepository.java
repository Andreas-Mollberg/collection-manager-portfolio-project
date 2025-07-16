package com.example.collection_manager.repositories;

import com.example.collection_manager.enums.Visibility;
import com.example.collection_manager.models.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CollectionRepository extends JpaRepository<Collection, Long> {

    List<Collection> findByUserIdAndVisibility(Long userId, Visibility visibility);

}

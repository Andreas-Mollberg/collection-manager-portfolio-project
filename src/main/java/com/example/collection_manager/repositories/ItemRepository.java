package com.example.collection_manager.repositories;

import com.example.collection_manager.models.Item;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    @EntityGraph(attributePaths = {"collection", "collection.user"})
    Optional<Item> findByIdAndCollectionId(Long itemId, Long collectionId);
}

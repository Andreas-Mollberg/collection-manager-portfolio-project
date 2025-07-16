package com.example.collection_manager.repositories;

import com.example.collection_manager.models.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {

    List<Image> findAllByItemsId(Long itemId);

}

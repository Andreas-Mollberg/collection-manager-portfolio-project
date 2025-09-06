package com.example.collection_manager.dtos;

import com.example.collection_manager.enums.Visibility;
import java.util.List;

public record CollectionDetailDTO(
        Long id,
        String collectionTitle,
        String ownerName,
        Visibility visibility,
        Long ownerId,
        String description,
        List<ItemDTO> items,
        List<TagDTO> tags
) { }
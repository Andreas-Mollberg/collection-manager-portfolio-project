package com.example.collection_manager.dtos;

import com.example.collection_manager.enums.Visibility;

public record CollectionSummaryDTO(
        Long id,
        String collectionTitle,
        String ownerName,
        Visibility visibility,
        String description,
        String coverImageUrl
) {}


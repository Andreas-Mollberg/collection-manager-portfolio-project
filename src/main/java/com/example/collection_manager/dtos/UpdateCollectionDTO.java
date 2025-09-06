package com.example.collection_manager.dtos;

import com.example.collection_manager.enums.Visibility;

public record UpdateCollectionDTO(
        String collectionTitle,
        Visibility visibility,
        String description
) {}

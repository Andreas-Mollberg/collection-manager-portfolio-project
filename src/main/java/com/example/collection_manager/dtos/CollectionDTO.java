package com.example.collection_manager.dtos;

import com.example.collection_manager.enums.Visibility;

import java.util.List;

public record CollectionDTO(Long id, String collectionTitle, List<ItemDTO> items, Visibility visibility) {

}
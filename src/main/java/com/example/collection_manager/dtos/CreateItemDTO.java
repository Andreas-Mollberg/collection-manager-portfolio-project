package com.example.collection_manager.dtos;

import java.util.List;

public record CreateItemDTO(String itemName, String description, List<String> tags, List<Long> imageIds) {}

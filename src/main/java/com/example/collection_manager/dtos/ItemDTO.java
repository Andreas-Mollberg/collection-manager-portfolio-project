package com.example.collection_manager.dtos;

import java.util.List;

public record ItemDTO (Long itemId, String itemName, String description, List<TagDTO> tags) {
}

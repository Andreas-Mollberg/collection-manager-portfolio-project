package com.example.collection_manager.dtos;

import java.util.List;

public record UserDTO (
        Long userId,
        String userName,
        List<CollectionSummaryDTO> collections){
}

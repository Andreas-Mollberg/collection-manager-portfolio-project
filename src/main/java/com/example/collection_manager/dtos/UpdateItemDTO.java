package com.example.collection_manager.dtos;

import java.util.List;

public record UpdateItemDTO(String itemName, String description, List<String> tags) {

}



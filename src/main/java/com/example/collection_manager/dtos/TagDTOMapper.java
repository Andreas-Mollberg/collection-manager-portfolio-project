package com.example.collection_manager.dtos;

import com.example.collection_manager.models.Tag;
import org.springframework.stereotype.Component;

@Component
public class TagDTOMapper {
    public TagDTO toDTO(Tag tag) {
        return new TagDTO(tag.getId(), tag.getTagName());
    }
}

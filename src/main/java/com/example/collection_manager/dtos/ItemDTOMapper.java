package com.example.collection_manager.dtos;

import com.example.collection_manager.models.Item;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ItemDTOMapper {

    private final TagDTOMapper tagDTOMapper;

    public ItemDTOMapper(TagDTOMapper tagDTOMapper) {
        this.tagDTOMapper = tagDTOMapper;
    }

    public ItemDTO toDTO(Item item) {
        List<TagDTO> tagDTOs = item.getTags().stream()
                .map(tagDTOMapper::toDTO)
                .collect(Collectors.toList());

        return new ItemDTO(item.getId(), item.getItemName(), item.getDescription(), tagDTOs);
    }
}

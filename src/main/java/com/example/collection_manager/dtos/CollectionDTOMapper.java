package com.example.collection_manager.dtos;

import com.example.collection_manager.models.Collection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CollectionDTOMapper {

    @Autowired
    private ItemDTOMapper itemDTOMapper;

    @Autowired
    private TagDTOMapper tagDTOMapper;

    public CollectionDTO toDTO(Collection collection) {
        List<ItemDTO> items = collection.getItems() == null ? List.of() :
                collection.getItems().stream()
                        .map(itemDTOMapper::toDTO)
                        .toList();

        List<TagDTO> tags = collection.getTags() == null ? List.of() :
                collection.getTags().stream().map(tagDTOMapper::toDTO).toList();

        return new CollectionDTO(
                collection.getId(),
                collection.getCollectionTitle(),
                items,
                tags,
                collection.getVisibility()
        );
    }
}

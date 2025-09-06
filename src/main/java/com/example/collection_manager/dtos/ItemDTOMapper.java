package com.example.collection_manager.dtos;

import com.example.collection_manager.models.Item;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class ItemDTOMapper {

    private final TagDTOMapper tagDTOMapper;

    public ItemDTOMapper(TagDTOMapper tagDTOMapper) {
        this.tagDTOMapper = tagDTOMapper;
    }

    public ItemDTO toDTO(Item item) {
        if (item == null) {
            return null;
        }

        return new ItemDTO(
                item.getId(),
                item.getItemName(),
                item.getDescription(),
                mapTags(item),
                mapImages(item),
                getCollectionId(item),
                getCollectionTitle(item),
                getOwnerName(item)
        );
    }

    private List<TagDTO> mapTags(Item item) {
        if (item.getTags() == null) {
            return List.of();
        }
        return item.getTags().stream()
                .map(tagDTOMapper::toDTO)
                .toList();
    }

    private List<ImageDTO> mapImages(Item item) {
        if (item.getImages() == null) {
            return List.of();
        }
        return item.getImages().stream()
                .map(image -> new ImageDTO(image.getId(), image.getFileName(), image.getImageTitle()))
                .toList();
    }

    private Long getCollectionId(Item item) {
        return item.getCollection() != null ? item.getCollection().getId() : null;
    }

    private String getCollectionTitle(Item item) {
        return item.getCollection() != null ? item.getCollection().getCollectionTitle() : null;
    }

    private String getOwnerName(Item item) {
        return item.getCollection() != null && item.getCollection().getUser() != null
                ? item.getCollection().getUser().getUserName()
                : null;
    }
}


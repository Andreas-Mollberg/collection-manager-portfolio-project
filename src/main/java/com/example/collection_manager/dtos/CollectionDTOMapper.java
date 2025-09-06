package com.example.collection_manager.dtos;

import com.example.collection_manager.models.Collection;
import com.example.collection_manager.models.Item;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class CollectionDTOMapper {

    private final ItemDTOMapper itemDTOMapper;
    private final TagDTOMapper tagDTOMapper;

    public CollectionDTOMapper(ItemDTOMapper itemDTOMapper, TagDTOMapper tagDTOMapper) {
        this.itemDTOMapper = itemDTOMapper;
        this.tagDTOMapper = tagDTOMapper;
    }

    public CollectionSummaryDTO toSummaryDTO(Collection collection, boolean includeOwner) {
        if (collection == null) {
            return null;
        }

        String coverImageUrl = getCoverImageUrl(collection);
        String ownerName = includeOwner ? getOwnerName(collection) : null;

        return new CollectionSummaryDTO(
                collection.getId(),
                collection.getCollectionTitle(),
                ownerName,
                collection.getVisibility(),
                collection.getDescription(),
                coverImageUrl
        );
    }

    public CollectionDetailDTO toDetailDTO(Collection collection) {
        if (collection == null) {
            return null;
        }

        List<ItemDTO> items = mapItems(collection);
        List<TagDTO> tags = mapTags(collection);

        return new CollectionDetailDTO(
                collection.getId(),
                collection.getCollectionTitle(),
                getOwnerName(collection),
                collection.getVisibility(),
                getOwnerId(collection),
                collection.getDescription(),
                items,
                tags
        );
    }

    private String getCoverImageUrl(Collection collection) {
        if (collection.getItems() == null || collection.getItems().isEmpty()) {
            return null;
        }

        Item firstItem = collection.getItems().get(0);
        if (firstItem.getImages() == null || firstItem.getImages().isEmpty()) {
            return null;
        }

        String fileName = firstItem.getImages().get(0).getFileName();
        return fileName != null ? "/uploads/images/" + fileName : null;
    }

    private String getOwnerName(Collection collection) {
        return collection.getUser() != null ? collection.getUser().getUserName() : null;
    }

    private Long getOwnerId(Collection collection) {
        return collection.getUser() != null ? collection.getUser().getId() : null;
    }

    private List<ItemDTO> mapItems(Collection collection) {
        if (collection.getItems() == null) {
            return List.of();
        }
        return collection.getItems().stream()
                .map(itemDTOMapper::toDTO)
                .toList();
    }

    private List<TagDTO> mapTags(Collection collection) {
        if (collection.getTags() == null) {
            return List.of();
        }
        return collection.getTags().stream()
                .map(tagDTOMapper::toDTO)
                .toList();
    }
}



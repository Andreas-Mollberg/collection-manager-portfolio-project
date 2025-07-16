package com.example.collection_manager.services;

import com.example.collection_manager.dtos.ItemDTO;
import com.example.collection_manager.dtos.ItemDTOMapper;
import com.example.collection_manager.dtos.UpdateItemDTO;
import com.example.collection_manager.models.Collection;
import com.example.collection_manager.models.Image;
import com.example.collection_manager.models.Item;
import com.example.collection_manager.models.Tag;
import com.example.collection_manager.repositories.CollectionRepository;
import com.example.collection_manager.repositories.ImageRepository;
import com.example.collection_manager.repositories.ItemRepository;
import com.example.collection_manager.repositories.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ItemService {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ItemDTOMapper itemDTOMapper;

    @Autowired
    private CollectionRepository collectionRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private ImageRepository imageRepository;

    public Item saveItem(Item item) {
        return itemRepository.save(item);
    }

    public Optional<ItemDTO> findItemById(Long id) {
        return itemRepository.findById(id)
                .map(itemDTOMapper::toDTO);
    }

    public List<ItemDTO> findAllItems() {
        return itemRepository.findAll()
                .stream()
                .map(itemDTOMapper::toDTO)
                .collect(Collectors.toList());
    }

    public Optional<Item> updateItem(Long itemId, UpdateItemDTO dto) {
        Optional<Item> itemOpt = itemRepository.findById(itemId);
        if (itemOpt.isEmpty()) return Optional.empty();

        Item item = itemOpt.get();
        if (dto.itemName() != null) item.setItemName(dto.itemName());
        if (dto.description() != null) item.setDescription(dto.description());
        if (dto.tags() != null) {
            List<Tag> tags = resolveTags(new HashSet<>(dto.tags()));
            item.setTags(tags);
        }


        return Optional.of(itemRepository.save(item));
    }

    public Optional<Item> addItemToCollection(Long userId, Long collectionId, Item item, List<String> tagNames, List<Long> imageIds) {
        Optional<Collection> collectionOpt = collectionRepository.findById(collectionId);

        if (collectionOpt.isEmpty() || !collectionOpt.get().getUser().getId().equals(userId)) {
            return Optional.empty();
        }

        Collection collection = collectionOpt.get();
        item.setCollection(collection);

        Set<String> safeTagNames = tagNames != null ? new HashSet<>(tagNames) : Collections.emptySet();
        List<Tag> resolvedTags = resolveTags(safeTagNames);
        item.setTags(new ArrayList<>(resolvedTags));

        if (imageIds != null && !imageIds.isEmpty()) {
            List<Image> images = imageRepository.findAllById(imageIds);
            item.setImages(images);
        }

        return Optional.of(itemRepository.save(item));
    }

    private List<Tag> resolveTags(Set<String> tagNames) {
        List<String> normalized = tagNames.stream()
                .map(name -> name.trim().toLowerCase())
                .distinct()
                .toList();

        List<Tag> existingTags = tagRepository.findAllByTagNameInIgnoreCase(normalized);

        Map<String, Tag> existingTagMap = existingTags.stream()
                .collect(Collectors.toMap(
                        tag -> tag.getTagName().toLowerCase(),
                        tag -> tag
                ));

        List<Tag> resolvedTags = new ArrayList<>();

        for (String name : normalized) {
            if (existingTagMap.containsKey(name)) {
                resolvedTags.add(existingTagMap.get(name));
            } else {
                Tag newTag = new Tag(name);
                tagRepository.save(newTag);
                resolvedTags.add(newTag);
            }
        }

        return resolvedTags;
    }

    public List<Item> findItemsByCollectionId(Long collectionId) {
        return itemRepository.findByCollectionId(collectionId);
    }

    public Optional<Item> findItemByIdInCollection(Long itemId, Long collectionId) {
        return itemRepository.findByIdAndCollectionId(itemId, collectionId);
    }

    public Optional<Item> updateItemTags(Long userId, Long collectionId, Long itemId, List<Long> tagIds) {
        Optional<Item> optionalItem = itemRepository.findByIdAndCollectionId(itemId, collectionId);

        if (optionalItem.isEmpty()) return Optional.empty();

        Item item = optionalItem.get();

        if (!item.getCollection().getUser().getId().equals(userId)) return Optional.empty();

        List<Tag> tags = tagRepository.findAllById(tagIds);
        item.setTags(tags);

        return Optional.of(itemRepository.save(item));
    }

    public boolean deleteItemInCollection(Long itemId, Long collectionId) {
        Optional<Item> item = findItemByIdInCollection(itemId, collectionId);
        item.ifPresent(itemRepository::delete);
        return item.isPresent();
    }

}

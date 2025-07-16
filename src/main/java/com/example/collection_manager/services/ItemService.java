package com.example.collection_manager.services;

import com.example.collection_manager.dtos.ItemDTO;
import com.example.collection_manager.dtos.ItemDTOMapper;
import com.example.collection_manager.models.Collection;
import com.example.collection_manager.models.Item;
import com.example.collection_manager.models.Tag;
import com.example.collection_manager.repositories.CollectionRepository;
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

    public void updateItem(Item item) {
        itemRepository.save(item);
    }

    public void deleteItem(Long id) {
        itemRepository.deleteById(id);
    }

    public Optional<Item> addItemToCollection(Long userId, Long collectionId, Item item, List<String> tagNames) {
        Optional<Collection> collectionOpt = collectionRepository.findById(collectionId);

        if (collectionOpt.isEmpty() || !collectionOpt.get().getUser().getId().equals(userId)) {
            return Optional.empty();
        }

        Collection collection = collectionOpt.get();
        item.setCollection(collection);

        Set<String> safeTagNames = tagNames != null ? new HashSet<>(tagNames) : Collections.emptySet();
        List<Tag> resolvedTags = resolveTags(safeTagNames);
        item.setTags(new ArrayList<>(resolvedTags));

        return Optional.of(itemRepository.save(item));
    }

    private List<Tag> resolveTags(Set<String> tagNames) {
        // Normalize: lowercase & trim
        List<String> normalized = tagNames.stream()
                .map(name -> name.trim().toLowerCase())
                .distinct()
                .toList();

        // Fetch existing tags in bulk
        List<Tag> existingTags = tagRepository.findAllByTagNameInIgnoreCase(normalized);

        Map<String, Tag> existingTagMap = existingTags.stream()
                .collect(Collectors.toMap(
                        tag -> tag.getTagName().toLowerCase(), // key by lowercase
                        tag -> tag
                ));

        List<Tag> resolvedTags = new ArrayList<>();

        for (String name : normalized) {
            if (existingTagMap.containsKey(name)) {
                resolvedTags.add(existingTagMap.get(name));
            } else {
                Tag newTag = new Tag(name); // already lowercase
                tagRepository.save(newTag); // persist immediately
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

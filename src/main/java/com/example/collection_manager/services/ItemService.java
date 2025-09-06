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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ItemService {

    private final ItemRepository itemRepository;
    private final ItemDTOMapper itemDTOMapper;
    private final CollectionRepository collectionRepository;
    private final TagService tagService;
    private final ImageService imageService;
    private final ImageRepository imageRepository;
    private final AuthorizationService authorizationService;

    public ItemService(ItemRepository itemRepository,
                       ItemDTOMapper itemDTOMapper,
                       CollectionRepository collectionRepository,
                       TagService tagService,
                       ImageService imageService,
                       ImageRepository imageRepository,
                       AuthorizationService authorizationService) {
        this.itemRepository = itemRepository;
        this.itemDTOMapper = itemDTOMapper;
        this.collectionRepository = collectionRepository;
        this.tagService = tagService;
        this.imageService = imageService;
        this.imageRepository = imageRepository;
        this.authorizationService = authorizationService;
    }

    @Transactional
    public Optional<Item> updateItem(Long itemId, UpdateItemDTO dto) {
        return itemRepository.findById(itemId).map(existing -> {

            existing.setItemName(dto.itemName());
            existing.setDescription(dto.description());

            List<Tag> updatedTags = tagService.findTagsByIds(dto.tagIds());
            existing.getTags().clear();
            existing.getTags().addAll(updatedTags);

            List<Image> updatedImages = imageService.findImagesByIds(dto.imageIds());
            List<Image> oldImages     = new ArrayList<>(existing.getImages()); // keep track of original

            existing.getImages().clear();
            existing.getImages().addAll(updatedImages);

            Item saved = itemRepository.save(existing);

            for (Image image : oldImages) {
                if (!updatedImages.contains(image)) {
                    imageService.deleteImageIfOrphaned(image);
                }
            }

            return saved;
        });
    }

    @Transactional
    public Optional<Item> addItemToCollection(Long collectionId, Item item, List<Tag> tags, List<Image> images, Principal principal) {
        Optional<Collection> collectionOpt = collectionRepository.findById(collectionId);
        if (collectionOpt.isEmpty()) return Optional.empty();

        if (!authorizationService.isOwnerOfCollection(collectionId, principal)) return Optional.empty();

        Collection collection = collectionOpt.get();
        item.setCollection(collection);
        item.setTags(tags != null ? new ArrayList<>(tags) : new ArrayList<>());
        item.setImages(images != null ? new ArrayList<>(images) : new ArrayList<>());

        return Optional.of(itemRepository.save(item));
    }

    @Transactional(readOnly = true)
    public Optional<Item> findItemByIdInCollection(Long itemId, Long collectionId) {
        return itemRepository.findByIdAndCollectionId(itemId, collectionId);
    }

    @Transactional
    public boolean deleteItemInCollection(Long itemId, Long collectionId) {
        Optional<Item> optionalItem = itemRepository.findByIdAndCollectionId(itemId, collectionId);
        if (optionalItem.isEmpty()) return false;

        Item item = optionalItem.get();

        for (Image img : new ArrayList<>(item.getImages())) {
            item.getImages().remove(img);
            imageService.deleteImageIfOrphaned(img);
        }

        itemRepository.delete(item);
        return true;
    }

    @Transactional
    public void removeImageFromItem(Long itemId, Long imageId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("Image not found"));

        item.getImages().remove(image);
        itemRepository.save(item);

        imageService.deleteImageIfOrphaned(image);
    }

}

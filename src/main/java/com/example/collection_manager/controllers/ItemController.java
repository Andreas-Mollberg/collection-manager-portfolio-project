package com.example.collection_manager.controllers;

import com.example.collection_manager.dtos.CreateItemDTO;
import com.example.collection_manager.dtos.ItemDTO;
import com.example.collection_manager.dtos.ItemDTOMapper;
import com.example.collection_manager.dtos.UpdateItemDTO;
import com.example.collection_manager.models.Image;
import com.example.collection_manager.models.Item;
import com.example.collection_manager.models.Tag;
import com.example.collection_manager.repositories.ImageRepository;
import com.example.collection_manager.services.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping("/api/users/{userId}/collections/{collectionId}/items")
public class ItemController {

    private final ItemService itemService;
    private final ItemDTOMapper itemDTOMapper;
    private final TagService tagService;
    private final ImageRepository imageRepository;
    private final AuthorizationService authorizationService;

    public ItemController(ItemService itemService,
                          ItemDTOMapper itemDTOMapper,
                          TagService tagService,
                          ImageRepository imageRepository,
                          AuthorizationService authorizationService) {
        this.itemService = itemService;
        this.itemDTOMapper = itemDTOMapper;
        this.tagService = tagService;
        this.imageRepository = imageRepository;
        this.authorizationService = authorizationService;
    }

    @PostMapping
    public ResponseEntity<ItemDTO> addItemToCollection(
            @PathVariable Long userId,
            @PathVariable Long collectionId,
            @Valid @RequestBody CreateItemDTO dto,
            Principal principal) {

        if (!authorizationService.isOwnerOfCollection(collectionId, principal)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Item item = new Item();
        item.setItemName(dto.itemName());
        item.setDescription(dto.description());

        List<Tag> resolvedTags = tagService.getOrCreateTags(new HashSet<>(dto.tags()));
        List<Image> images = imageRepository.findAllById(dto.imageIds());

        return itemService.addItemToCollection(collectionId, item, resolvedTags, images, principal)
                .map(saved -> {
                    ItemDTO body = itemDTOMapper.toDTO(saved);
                    URI location = URI.create(
                            "/api/users/" + userId + "/collections/" + collectionId + "/items/" + saved.getId());
                    return ResponseEntity.created(location).body(body);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<ItemDTO> getItemById(
            @PathVariable Long collectionId,
            @PathVariable Long itemId,
            Principal principal) {

        if (!authorizationService.isOwnerOfItem(itemId, principal)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return itemService.findItemByIdInCollection(itemId, collectionId)
                .map(itemDTOMapper::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{itemId}")
    public ResponseEntity<ItemDTO> updateItemInCollection(
            @PathVariable Long collectionId,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateItemDTO dto,
            Principal principal) {

        if (!authorizationService.isOwnerOfItem(itemId, principal)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return itemService.updateItem(itemId, dto)
                .map(updated -> ResponseEntity.ok(itemDTOMapper.toDTO(updated)))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> deleteItem(
            @PathVariable Long collectionId,
            @PathVariable Long itemId,
            Principal principal) {

        if (!authorizationService.isOwnerOfItem(itemId, principal)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        boolean deleted = itemService.deleteItemInCollection(itemId, collectionId);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
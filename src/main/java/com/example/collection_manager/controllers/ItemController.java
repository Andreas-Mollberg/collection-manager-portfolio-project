package com.example.collection_manager.controllers;

import com.example.collection_manager.dtos.CreateItemDTO;
import com.example.collection_manager.dtos.ItemDTO;
import com.example.collection_manager.dtos.ItemDTOMapper;
import com.example.collection_manager.models.Item;
import com.example.collection_manager.services.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/{userId}/collections/{collectionId}/items")
public class ItemController {

    @Autowired
    private ItemService itemService;

    @Autowired
    private ItemDTOMapper itemDTOMapper;

    @PostMapping
    public ResponseEntity<ItemDTO> addItemToCollection(
            @PathVariable Long userId,
            @PathVariable Long collectionId,
            @RequestBody CreateItemDTO createItemDTO
    ) {
        Item item = new Item();
        item.setItemName(createItemDTO.itemName());
        item.setDescription(createItemDTO.description());

        return itemService.addItemToCollection(userId, collectionId, item, createItemDTO.tags())
                .map(savedItem -> ResponseEntity.ok(itemDTOMapper.toDTO(savedItem)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<ItemDTO> getItemById(
            @PathVariable Long userId,
            @PathVariable Long collectionId,
            @PathVariable Long itemId
    ) {
        return itemService.findItemByIdInCollection(itemId, collectionId)
                .map(itemDTOMapper::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}

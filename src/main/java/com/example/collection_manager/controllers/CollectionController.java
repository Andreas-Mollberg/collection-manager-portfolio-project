package com.example.collection_manager.controllers;

import com.example.collection_manager.dtos.CollectionDTO;
import com.example.collection_manager.dtos.CollectionDTOMapper;
import com.example.collection_manager.models.Collection;
import com.example.collection_manager.services.CollectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users/{userId}/collections")
public class CollectionController {

    @Autowired
    private CollectionService collectionService;

    @Autowired
    private CollectionDTOMapper collectionDTOMapper;

    @PostMapping
    public ResponseEntity<CollectionDTO> createCollection(
            @PathVariable Long userId,
            @RequestBody Collection collection) {
        return collectionService.createCollectionForUser(userId, collection)
                .map(saved -> ResponseEntity.ok(collectionDTOMapper.toDTO(saved)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<CollectionDTO> getCollections(@PathVariable Long userId) {
        return collectionService.getCollectionsByUserId(userId)
                .stream()
                .map(collectionDTOMapper::toDTO)
                .toList();
    }

    @GetMapping("/{collectionId}")
    public ResponseEntity<CollectionDTO> getCollectionById(
            @PathVariable Long userId,
            @PathVariable Long collectionId,
            @RequestParam(name = "viewerId", required = false) Long viewerId
    ) {
        Optional<Collection> collectionOpt = collectionService.getCollectionById(collectionId);

        if (collectionOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Collection collection = collectionOpt.get();

        boolean isOwner = viewerId != null && viewerId.equals(collection.getUser().getId());

        switch (collection.getVisibility()) {
            case PUBLIC -> { return ResponseEntity.ok(collectionDTOMapper .toDTO(collection)); }
            case PRIVATE, FRIENDS -> {
                if (isOwner) {
                    return ResponseEntity.ok(collectionDTOMapper .toDTO(collection));
                } else {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            }
            default -> {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
    }


    @DeleteMapping("/{collectionId}")
    public ResponseEntity<Void> deleteCollection(
            @PathVariable Long userId,
            @PathVariable Long collectionId) {
        boolean deleted = collectionService.deleteCollection(collectionId);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}

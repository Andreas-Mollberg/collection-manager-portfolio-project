package com.example.collection_manager.controllers;

import com.example.collection_manager.dtos.CollectionDetailDTO;
import com.example.collection_manager.dtos.CollectionSummaryDTO;
import com.example.collection_manager.dtos.CreateCollectionDTO;
import com.example.collection_manager.dtos.UpdateCollectionDTO;
import com.example.collection_manager.services.AuthorizationService;
import com.example.collection_manager.services.CollectionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Validated
@RestController
@RequestMapping("/api/users/{userId}/collections")
public class CollectionController {

    private final CollectionService collectionService;
    private final AuthorizationService authorizationService;

    public CollectionController(CollectionService collectionService, AuthorizationService authorizationService) {
        this.collectionService = collectionService;
        this.authorizationService = authorizationService;
    }

    @PostMapping
    public ResponseEntity<CollectionDetailDTO> createCollection(
            @PathVariable Long userId,
            @Valid @RequestBody CreateCollectionDTO dto,
            Principal principal) {

        if (!authorizationService.isSameUser(userId, principal)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return collectionService.createCollectionForUser(userId, dto)
                .map(c -> {
                    Optional<CollectionDetailDTO> body = collectionService.getCollectionById(c.getId());
                    URI location = URI.create("/api/users/" + userId + "/collections/" + c.getId());
                    return body.map(collection -> ResponseEntity.created(location).body(collection))
                            .orElseGet(() -> ResponseEntity.created(location).build());
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<CollectionSummaryDTO>> getCollections(
            @PathVariable Long userId,
            Principal principal) {

        if (!authorizationService.isSameUser(userId, principal)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(collectionService.getCollectionsByUserId(userId));
    }

    @GetMapping("/{collectionId}")
    public ResponseEntity<CollectionDetailDTO> getCollectionById(
            @PathVariable Long userId,
            @PathVariable Long collectionId,
            Principal principal) {

        Optional<CollectionDetailDTO> opt = collectionService.getCollectionById(collectionId);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        CollectionDetailDTO dto = opt.get();
        String username = (principal != null) ? principal.getName() : null;

        boolean isOwner = (username != null) && username.equals(dto.ownerName());
        boolean isPublic = dto.visibility() == com.example.collection_manager.enums.Visibility.PUBLIC;
        boolean isFriendVisible = dto.visibility() == com.example.collection_manager.enums.Visibility.FRIENDS
                && authorizationService.isFriendsWith(dto.ownerId(), principal);

        if (isOwner || isPublic || isFriendVisible) {
            return ResponseEntity.ok(dto);
        }

        // Not allowed: hide existence
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{collectionId}")
    public ResponseEntity<Void> deleteCollection(
            @PathVariable Long userId,
            @PathVariable Long collectionId,
            Principal principal) {

        if (!authorizationService.isOwnerOfCollection(collectionId, principal)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (findIfPathOwnerMatches(userId, collectionId).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        boolean deleted = collectionService.deleteCollection(collectionId, principal);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @PutMapping("/{collectionId}")
    public ResponseEntity<Void> updateCollection(
            @PathVariable Long userId,
            @PathVariable Long collectionId,
            @Valid @RequestBody UpdateCollectionDTO dto,
            Principal principal) {

        if (!authorizationService.isOwnerOfCollection(collectionId, principal)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (findIfPathOwnerMatches(userId, collectionId).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        boolean updated = collectionService.updateCollection(
                collectionId, dto.collectionTitle(), dto.visibility(), dto.description(), principal);

        return updated ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/public")
    public List<CollectionSummaryDTO> getAllPublicCollections() {
        return collectionService.getAllPublicCollections();
    }

    private Optional<CollectionDetailDTO> findIfPathOwnerMatches(Long userId, Long collectionId) {
        Optional<CollectionDetailDTO> opt = collectionService.getCollectionById(collectionId);
        if (opt.isPresent() && userId.equals(opt.get().ownerId())) {
            return opt;
        }
        return Optional.empty();
    }
}

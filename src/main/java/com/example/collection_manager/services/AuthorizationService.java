package com.example.collection_manager.services;

import com.example.collection_manager.repositories.UserRepository;
import com.example.collection_manager.repositories.CollectionRepository;
import com.example.collection_manager.repositories.ItemRepository;
import org.springframework.stereotype.Service;
import java.security.Principal;

@Service
public class AuthorizationService {

    private final CollectionRepository collectionRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final FriendService friendService;

    public AuthorizationService(CollectionRepository collectionRepository,
                                ItemRepository itemRepository,
                                UserRepository userRepository,
                                FriendService friendService) {
        this.collectionRepository = collectionRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.friendService = friendService;
    }

    public boolean isAuthenticated(Principal principal) {
        return principal != null;
    }

    public boolean hasUsername(Principal principal, String expectedUsername) {
        return principal != null &&
                expectedUsername != null &&
                expectedUsername.equals(principal.getName());
    }

    public boolean isSameUser(Long userId, Principal principal) {
        if (principal == null || userId == null) return false;
        return userRepository.findById(userId)
                .map(u -> u.getUserName().equals(principal.getName()))
                .orElse(false);
    }

    public boolean isOwnerOfCollection(Long collectionId, Principal principal) {
        if (principal == null || collectionId == null) return false;
        return collectionRepository.findById(collectionId)
                .map(c -> hasUsername(principal, c.getUser().getUserName()))
                .orElse(false);
    }

    public boolean isOwnerOfItem(Long itemId, Principal principal) {
        if (principal == null || itemId == null) return false;
        return itemRepository.findById(itemId)
                .map(i -> hasUsername(principal, i.getCollection().getUser().getUserName()))
                .orElse(false);
    }

    public boolean areFriends(Long userAId, Long userBId) {
        return userAId != null && userBId != null && friendService.areFriends(userAId, userBId);
    }

    public boolean isFriendsWith(Long targetUserId, Principal principal) {
        if (principal == null || targetUserId == null) return false;
        return userRepository.findByUserName(principal.getName())
                .map(u -> areFriends(u.getId(), targetUserId))
                .orElse(false);
    }

    public boolean canViewCollection(Long collectionId, Principal principal) {
        if (collectionId == null) return false;
        return collectionRepository.findById(collectionId).map(c -> {
            var owner = c.getUser();
            if (owner == null) return false;

            return switch (c.getVisibility()) {
                case PUBLIC -> true;
                case PRIVATE -> hasUsername(principal, owner.getUserName());
                case FRIENDS -> hasUsername(principal, owner.getUserName())
                        || isFriendsWith(owner.getId(), principal);
            };
        }).orElse(false);
    }
}


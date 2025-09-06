package com.example.collection_manager.controllers;

import com.example.collection_manager.dtos.FriendDTO;
import com.example.collection_manager.dtos.FriendDTOMapper;
import com.example.collection_manager.services.AuthorizationService;
import com.example.collection_manager.services.FriendService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/friends")
public class FriendController {

    private final FriendService friendService;
    private final AuthorizationService authorizationService;
    private final FriendDTOMapper friendDTOMapper;

    public FriendController(FriendService friendService,
                            AuthorizationService authorizationService,
                            FriendDTOMapper friendDTOMapper) {
        this.friendService = friendService;
        this.authorizationService = authorizationService;
        this.friendDTOMapper = friendDTOMapper;
    }

    @PostMapping("/{targetUserId}")
    public ResponseEntity<Void> sendRequest(
            @PathVariable Long userId,
            @PathVariable Long targetUserId,
            Principal principal) {

        if (!authorizationService.isSameUser(userId, principal)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (userId.equals(targetUserId)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        boolean success = friendService.sendRequest(userId, targetUserId);
        return success
                ? ResponseEntity.noContent().build()
                : ResponseEntity.status(HttpStatus.CONFLICT).build();
    }

    @PostMapping("/{requesterId}/accept")
    public ResponseEntity<Void> acceptRequest(
            @PathVariable Long userId,
            @PathVariable Long requesterId,
            Principal principal) {

        if (!authorizationService.isSameUser(userId, principal)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        boolean success = friendService.acceptRequest(requesterId, userId);
        return success
                ? ResponseEntity.noContent().build()
                : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @PostMapping("/{otherUserId}/decline")
    public ResponseEntity<Void> declineOrCancel(
            @PathVariable Long userId,
            @PathVariable Long otherUserId,
            Principal principal) {

        if (!authorizationService.isSameUser(userId, principal)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        boolean success = friendService.declineOrCancel(otherUserId, userId);
        return success
                ? ResponseEntity.noContent().build()
                : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @DeleteMapping("/{otherUserId}")
    public ResponseEntity<Void> removeFriendship(
            @PathVariable Long userId,
            @PathVariable Long otherUserId,
            Principal principal) {

        if (!authorizationService.isSameUser(userId, principal)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        boolean success = friendService.removeFriend(userId, otherUserId);
        return success
                ? ResponseEntity.noContent().build()
                : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @GetMapping
    public ResponseEntity<List<FriendDTO>> listFriends(
            @PathVariable Long userId,
            Principal principal) {

        if (!authorizationService.isSameUser(userId, principal)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<FriendDTO> list = friendService.friendsOf(userId)
                .stream()
                .map(friendDTOMapper::toDTO)
                .toList();

        return ResponseEntity.ok(list);
    }

    @GetMapping("/requests/incoming")
    public ResponseEntity<List<FriendDTO>> incomingRequests(
            @PathVariable Long userId,
            Principal principal) {

        if (!authorizationService.isSameUser(userId, principal)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<FriendDTO> list = friendService.incomingRequests(userId)
                .stream()
                .map(friendDTOMapper::toDTO)
                .toList();

        return ResponseEntity.ok(list);
    }

    @GetMapping("/requests/outgoing")
    public ResponseEntity<List<FriendDTO>> outgoingRequests(
            @PathVariable Long userId,
            Principal principal) {

        if (!authorizationService.isSameUser(userId, principal)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<FriendDTO> list = friendService.outgoingRequests(userId)
                .stream()
                .map(friendDTOMapper::toDTO)
                .toList();

        return ResponseEntity.ok(list);
    }
}

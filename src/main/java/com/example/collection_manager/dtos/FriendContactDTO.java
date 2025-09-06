package com.example.collection_manager.dtos;

import com.example.collection_manager.enums.FriendStatus;

public record FriendContactDTO(
        Long friendId,
        Long otherUserId,
        String otherUserName,
        FriendStatus status
) { }

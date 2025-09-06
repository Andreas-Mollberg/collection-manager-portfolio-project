package com.example.collection_manager.dtos;

import com.example.collection_manager.enums.FriendStatus;

public record FriendDTO(
        Long id,
        Long requesterId,
        String requesterName,
        Long recipientId,
        String recipientName,
        FriendStatus status
) { }


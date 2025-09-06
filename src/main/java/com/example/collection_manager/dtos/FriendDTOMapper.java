package com.example.collection_manager.dtos;

import com.example.collection_manager.models.Friend;
import org.springframework.stereotype.Component;

@Component
public class FriendDTOMapper {

    public FriendDTO toDTO(Friend friend) {
        if (friend == null) {
            return null;
        }

        return new FriendDTO(
                friend.getId(),
                getRequesterId(friend),
                getRequesterName(friend),
                getRecipientId(friend),
                getRecipientName(friend),
                friend.getStatus()
        );
    }

    private Long getRequesterId(Friend friend) {
        return friend.getRequester() != null ? friend.getRequester().getId() : null;
    }

    private String getRequesterName(Friend friend) {
        return friend.getRequester() != null ? friend.getRequester().getUserName() : null;
    }

    private Long getRecipientId(Friend friend) {
        return friend.getRecipient() != null ? friend.getRecipient().getId() : null;
    }

    private String getRecipientName(Friend friend) {
        return friend.getRecipient() != null ? friend.getRecipient().getUserName() : null;
    }
}


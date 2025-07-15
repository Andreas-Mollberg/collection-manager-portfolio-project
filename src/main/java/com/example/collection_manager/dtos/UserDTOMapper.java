package com.example.collection_manager.dtos;

import com.example.collection_manager.models.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserDTOMapper {

    public UserDTO toDTO(User user) {
        List<CollectionDTO> collectionDTOs = user.getCollections().stream()
                .map(c -> new CollectionDTO(c.getId(), c.getCollectionTitle()))
                .collect(Collectors.toList());

        return new UserDTO(
                user.getId(),
                user.getUserName(),
                collectionDTOs
        );
    }
}


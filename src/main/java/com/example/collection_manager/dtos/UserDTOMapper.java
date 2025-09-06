package com.example.collection_manager.dtos;

import com.example.collection_manager.models.User;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class UserDTOMapper {

    private final CollectionDTOMapper collectionDTOMapper;

    public UserDTOMapper(CollectionDTOMapper collectionDTOMapper) {
        this.collectionDTOMapper = collectionDTOMapper;
    }

    public UserDTO toDTO(User user) {
        if (user == null) {
            return null;
        }

        return new UserDTO(
                user.getId(),
                user.getUserName(),
                mapCollections(user)
        );
    }

    private List<CollectionSummaryDTO> mapCollections(User user) {
        if (user.getCollections() == null) {
            return List.of();
        }
        return user.getCollections().stream()
                .map(collection -> collectionDTOMapper.toSummaryDTO(collection, false))
                .toList();
    }
}






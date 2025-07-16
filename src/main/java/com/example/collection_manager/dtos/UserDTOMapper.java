package com.example.collection_manager.dtos;

import com.example.collection_manager.models.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserDTOMapper {

    private final ItemDTOMapper itemDTOMapper;

    public UserDTOMapper(ItemDTOMapper itemDTOMapper) {
        this.itemDTOMapper = itemDTOMapper;
    }

    public UserDTO toDTO(User user) {
        List<CollectionDTO> collectionDTOs =
                user.getCollections() == null ? List.of() :
                        user.getCollections().stream()
                                .map(c -> new CollectionDTO(
                                        c.getId(),
                                        c.getCollectionTitle(),
                                        c.getItems() == null ? List.of() :
                                                c.getItems().stream()
                                                        .map(itemDTOMapper::toDTO)
                                                        .collect(Collectors.toList()),
                                        c.getVisibility()
                                ))
                                .collect(Collectors.toList());

        return new UserDTO(user.getId(), user.getUserName(), collectionDTOs);
    }

}


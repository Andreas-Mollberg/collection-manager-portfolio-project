package com.example.collection_manager.services;

import com.example.collection_manager.models.Collection;
import com.example.collection_manager.repositories.CollectionRepository;
import com.example.collection_manager.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class CollectionService {

    @Autowired
    private CollectionRepository collectionRepository;

    @Autowired
    private UserRepository userRepository;

    public Optional<Collection> createCollectionForUser(Long userId, Collection collection) {
        return userRepository.findById(userId).map(user -> {
            collection.setUser(user);
            return collectionRepository.save(collection);
        });
    }

    public List<Collection> getCollectionsByUserId(Long userId) {
        return collectionRepository.findAll().stream()
                .filter(c -> c.getUser() != null && c.getUser().getId().equals(userId))
                .toList();
    }

    public Optional<Collection> getCollectionById(Long collectionId) {
        return collectionRepository.findById(collectionId);
    }

    public boolean deleteCollection(Long collectionId) {
        Optional<Collection> collection = collectionRepository.findById(collectionId);
        collection.ifPresent(collectionRepository::delete);
        return collection.isPresent();
    }
}

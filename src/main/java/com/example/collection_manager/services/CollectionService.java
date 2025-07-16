package com.example.collection_manager.services;

import com.example.collection_manager.dtos.CreateCollectionDTO;
import com.example.collection_manager.enums.Visibility;
import com.example.collection_manager.models.Collection;
import com.example.collection_manager.models.Tag;
import com.example.collection_manager.models.User;
import com.example.collection_manager.repositories.CollectionRepository;
import com.example.collection_manager.repositories.TagRepository;
import com.example.collection_manager.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class CollectionService {

    @Autowired
    private CollectionRepository collectionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TagRepository tagRepository;

    public Optional<Collection> createCollectionForUser(Long userId, CreateCollectionDTO dto) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) return Optional.empty();

        Collection collection = new Collection();
        collection.setCollectionTitle(dto.getCollectionTitle());
        collection.setVisibility(dto.getVisibility() != null ? dto.getVisibility() : Visibility.PRIVATE);
        collection.setUser(userOpt.get());

        if (dto.getTags() != null) {
            List<Tag> tags = resolveTags(new HashSet<>(dto.getTags()));
            collection.setTags(tags);
        }

        return Optional.of(collectionRepository.save(collection));
    }

    private List<Tag> resolveTags(Set<String> tagNames) {
        return tagNames.stream()
                .map(name -> tagRepository.findByTagName(name)
                        .orElseGet(() -> tagRepository.save(new Tag(name))))
                .toList();
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

    public List<Collection> getPublicCollectionsByUserId(Long userId) {
        return collectionRepository.findByUserIdAndVisibility(userId, Visibility.PUBLIC);
    }

}

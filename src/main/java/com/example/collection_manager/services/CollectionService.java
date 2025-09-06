package com.example.collection_manager.services;

import com.example.collection_manager.dtos.CollectionDetailDTO;
import com.example.collection_manager.dtos.CollectionSummaryDTO;
import com.example.collection_manager.dtos.CollectionDTOMapper;
import com.example.collection_manager.dtos.CreateCollectionDTO;
import com.example.collection_manager.enums.Visibility;
import com.example.collection_manager.models.*;
import com.example.collection_manager.repositories.CollectionRepository;
import com.example.collection_manager.repositories.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CollectionService {

    private final CollectionRepository collectionRepository;
    private final TagService tagService;
    private final UserRepository userRepository;
    private final ItemService itemService;
    private final CollectionDTOMapper collectionDTOMapper;
    private final FriendService friendService;
    private final AuthorizationService authorizationService;

    private static final int RECENT_WINDOW_DAYS = 1;

    public CollectionService(CollectionRepository collectionRepository,
                             TagService tagService,
                             UserRepository userRepository,
                             ItemService itemService,
                             CollectionDTOMapper collectionDTOMapper,
                             FriendService friendService,
                             AuthorizationService authorizationService) {
        this.collectionRepository = collectionRepository;
        this.tagService = tagService;
        this.userRepository = userRepository;
        this.itemService = itemService;
        this.collectionDTOMapper = collectionDTOMapper;
        this.friendService = friendService;
        this.authorizationService = authorizationService;
    }

    @Transactional
    public Optional<Collection> createCollectionForUser(Long userId, CreateCollectionDTO dto) {
        return userRepository.findById(userId).map(user -> {
            Collection c = new Collection();
            c.setCollectionTitle(dto.getCollectionTitle());
            c.setVisibility(dto.getVisibility() != null ? dto.getVisibility() : Visibility.PRIVATE);
            c.setDescription(dto.getDescription());
            c.setUser(user);
            c.setCreatedAt(LocalDateTime.now());
            c.setUpdatedAt(LocalDateTime.now());

            if (dto.getTags() != null) {
                List<Tag> tags = tagService.getOrCreateTags(dto.getTags());
                c.setTags(tags);
            }
            return collectionRepository.save(c);
        });
    }

    @Transactional(readOnly = true)
    public List<CollectionSummaryDTO> getCollectionsByUserId(Long userId) {
        return collectionRepository.findByUserId(userId)
                .stream()
                .map(c -> collectionDTOMapper.toSummaryDTO(c, false))
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<CollectionDetailDTO> getCollectionById(Long id) {
        return collectionRepository.findById(id)
                .map(collectionDTOMapper::toDetailDTO);
    }

    @Transactional
    public boolean deleteCollection(Long collectionId, Principal principal) {
        if (!authorizationService.isOwnerOfCollection(collectionId, principal)) {
            return false;
        }
        return collectionRepository.findById(collectionId).map(c -> {
            c.getItems().forEach(item -> itemService.deleteItemInCollection(item.getId(), collectionId));
            collectionRepository.delete(c);
            return true;
        }).orElse(false);
    }

    @Transactional
    public boolean updateCollection(Long id,
                                    String title,
                                    Visibility visibility,
                                    String description,
                                    Principal principal) {
        if (!authorizationService.isOwnerOfCollection(id, principal)) return false;
        return collectionRepository.findById(id).map(c -> {
            c.setCollectionTitle(title);
            c.setVisibility(visibility);
            c.setDescription(description);
            c.setUpdatedAt(LocalDateTime.now());
            collectionRepository.save(c);
            return true;
        }).orElse(false);
    }

    @Transactional(readOnly = true)
    public List<CollectionSummaryDTO> getPublicCollectionsByUserId(Long userId) {
        return collectionRepository.findByUserIdAndVisibility(userId, Visibility.PUBLIC)
                .stream().map(c -> collectionDTOMapper.toSummaryDTO(c, true)).toList();
    }

    @Transactional(readOnly = true)
    public List<CollectionSummaryDTO> getAllPublicCollections() {
        return collectionRepository.findByVisibility(Visibility.PUBLIC)
                .stream().map(c -> collectionDTOMapper.toSummaryDTO(c, true)).toList();
    }

    @Transactional(readOnly = true)
    public List<CollectionSummaryDTO> getFriendsCollectionsForUser(Long userId) {
        List<Long> ids = friendService.friendsOf(userId).stream()
                .map(f -> f.getRequester().getId().equals(userId) ? f.getRecipient().getId() : f.getRequester().getId())
                .distinct().toList();

        if (ids.isEmpty()) return List.of();

        return collectionRepository.findByUserIdInAndVisibility(ids, Visibility.FRIENDS)
                .stream().map(c -> collectionDTOMapper.toSummaryDTO(c, true)).toList();
    }

    @Transactional(readOnly = true)
    public List<CollectionSummaryDTO> findRecentCollectionsByUserId(Long userId, int limit) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(RECENT_WINDOW_DAYS);
        List<Collection> raw = collectionRepository
                .findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(userId, cutoff);

        if (limit > 0 && raw.size() > limit) {
            raw = raw.subList(0, limit);
        }

        return raw.stream()
                .map(c -> collectionDTOMapper.toSummaryDTO(c, false))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CollectionSummaryDTO> findRecentCollectionsVisibleToUser(Long userId, int limit) {
        var since = LocalDateTime.now().minusDays(RECENT_WINDOW_DAYS);
        var friends = friendIdsOf(userId);

        var page = PageRequest.of(0, Math.max(1, limit));
        var raw = collectionRepository.findRecentVisible(userId, friends, since, page);

        return raw.stream()
                .map(c -> collectionDTOMapper.toSummaryDTO(c, true))
                .toList();
    }

    @Transactional(readOnly = true)
    public int countRecentCollectionsVisibleToUser(Long userId) {
        var since = LocalDateTime.now().minusDays(RECENT_WINDOW_DAYS);
        var friends = friendIdsOf(userId);
        return (int) collectionRepository.countRecentVisible(userId, friends, since);
    }

    private List<Long> friendIdsOf(Long userId) {
        return friendService.friendsOf(userId).stream()
                .map(f -> f.getRequester().getId().equals(userId) ? f.getRecipient().getId() : f.getRequester().getId())
                .distinct()
                .toList();
    }
}

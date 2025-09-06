package com.example.collection_manager.services;

import com.example.collection_manager.models.Tag;
import com.example.collection_manager.repositories.TagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TagService {

    private final TagRepository tagRepository;

    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @Transactional
    public Tag createTag(Tag tag) {
        return tagRepository.save(tag);
    }

    @Transactional(readOnly = true)
    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }

    @Transactional
    public boolean deleteTag(Long id) {
        Optional<Tag> tag = tagRepository.findById(id);
        if (tag.isPresent()) {
            tagRepository.delete(tag.get());
            return true;
        }
        return false;
    }

    @Transactional(readOnly = true)
    public Optional<Tag> findByName(String name) {
        if (name == null) return Optional.empty();
        String normalized = name.trim();
        if (normalized.isEmpty()) return Optional.empty();
        return tagRepository.findByTagName(normalized);
    }

    @Transactional
    public List<Tag> getOrCreateTags(Collection<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) return List.of();

        Set<String> normalized = tagNames.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toLowerCase)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (normalized.isEmpty()) return List.of();

        List<Tag> existing = tagRepository.findAllByTagNameInIgnoreCase(new ArrayList<>(normalized));
        Map<String, Tag> byLowerName = existing.stream()
                .collect(Collectors.toMap(t -> t.getTagName().toLowerCase(), t -> t));

        List<Tag> toCreate = new ArrayList<>();
        for (String lower : normalized) {
            if (!byLowerName.containsKey(lower)) {
                toCreate.add(new Tag(lower));
            }
        }

        if (!toCreate.isEmpty()) {
            List<Tag> created = tagRepository.saveAll(toCreate);
            for (Tag t : created) {
                byLowerName.put(t.getTagName().toLowerCase(), t);
            }
        }

        List<Tag> result = new ArrayList<>(normalized.size());
        for (String lower : normalized) {
            result.add(byLowerName.get(lower));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<Tag> findTagsByIds(Collection<Long> ids) {
        return (ids == null || ids.isEmpty())
                ? List.of()
                : tagRepository.findAllById(ids);
    }
}


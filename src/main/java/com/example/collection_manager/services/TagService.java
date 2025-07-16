package com.example.collection_manager.services;

import com.example.collection_manager.models.Tag;
import com.example.collection_manager.repositories.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class TagService {

    @Autowired
    private TagRepository tagRepository;

    public Tag createTag(Tag tag) {
        return tagRepository.save(tag);
    }

    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }

    public boolean deleteTag(Long id) {
        Optional<Tag> tag = tagRepository.findById(id);
        if (tag.isPresent()) {
            tagRepository.delete(tag.get());
            return true;
        }
        return false;
    }

    public Optional<Tag> findByName(String name) {
        return tagRepository.findByTagName(name);
    }
}

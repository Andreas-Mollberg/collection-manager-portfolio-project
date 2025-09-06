package com.example.collection_manager.controllers;

import com.example.collection_manager.models.Tag;
import com.example.collection_manager.services.TagService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/tags")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @PostMapping
    public ResponseEntity<Tag> createTag(@Valid @RequestBody Tag tag) {
        Tag savedTag = tagService.createTag(tag);
        URI location = URI.create("/api/tags/" + savedTag.getId());
        return ResponseEntity.created(location).body(savedTag);
    }

    @GetMapping
    public List<Tag> getAllTags() {
        return tagService.getAllTags();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTag(@PathVariable Long id) {
        boolean deleted = tagService.deleteTag(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}

package com.example.collection_manager.controllers;

import com.example.collection_manager.dtos.ImageUploadDTO;
import com.example.collection_manager.dtos.ImageUploadRequestDTO;
import com.example.collection_manager.models.Image;
import com.example.collection_manager.services.ImageService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<Image> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            Image saved = imageService.uploadAndSave(file);
            URI location = URI.create("/api/images/" + saved.getId());
            return ResponseEntity.created(location).body(saved);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/metadata")
    public ResponseEntity<Image> saveImageMetadata(@Valid @RequestBody ImageUploadDTO dto) {
        Image saved = imageService.saveImageMetadata(dto);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/attach")
    public ResponseEntity<?> attachImageToItem(@Valid @RequestBody ImageUploadRequestDTO dto) {
        Optional<Image> saved = imageService.saveAndAttachImageToItem(dto.itemId(), dto.imageId(), dto.imageTitle());

        return saved
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Item not found"));
    }
}

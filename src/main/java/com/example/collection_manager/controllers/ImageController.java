package com.example.collection_manager.controllers;

import com.example.collection_manager.dtos.ImageUploadDTO;
import com.example.collection_manager.dtos.ImageUploadRequestDTO;
import com.example.collection_manager.models.Image;
import com.example.collection_manager.services.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    @Autowired
    private ImageService imageService;

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path uploadPath = Paths.get("src/main/resources/static/uploads/images");
            Files.createDirectories(uploadPath);

            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return ResponseEntity.ok(fileName);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Upload failed");
        }
    }

    @PostMapping("/metadata")
    public ResponseEntity<Image> saveImageMetadata(@RequestBody ImageUploadDTO dto) {
        Image saved = imageService.saveImageMetadata(dto);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/attach")
    public ResponseEntity<?> uploadAndAttachImage(@RequestBody ImageUploadRequestDTO dto) {
        Optional<Image> saved = imageService.saveAndAttachImageToItem(dto.itemId(), dto.fileName(), dto.imageTitle());
        return saved
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().body("Item not found"));
    }

}

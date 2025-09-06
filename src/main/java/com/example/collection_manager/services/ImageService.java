package com.example.collection_manager.services;

import com.example.collection_manager.dtos.ImageUploadDTO;
import com.example.collection_manager.models.Image;
import com.example.collection_manager.models.Item;
import com.example.collection_manager.repositories.ImageRepository;
import com.example.collection_manager.repositories.ItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@Service
public class ImageService {

    private final ImageRepository imageRepository;
    private final ItemRepository itemRepository;

    public ImageService(ImageRepository imageRepository, ItemRepository itemRepository) {
        this.imageRepository = imageRepository;
        this.itemRepository = itemRepository;
    }

    @Transactional
    public Image saveImageMetadata(ImageUploadDTO dto) {
        Image image = new Image();
        image.setFileName(dto.fileName());
        image.setImageTitle(dto.imageTitle() != null ? dto.imageTitle() : "Untitled");
        return imageRepository.save(image);
    }

    @Transactional
    public Optional<Image> saveAndAttachImageToItem(Long itemId, Long imageId, String imageTitle) {
        Optional<Item> itemOpt = itemRepository.findById(itemId);
        Optional<Image> imageOpt = imageRepository.findById(imageId);

        if (itemOpt.isEmpty() || imageOpt.isEmpty()) return Optional.empty();

        Item item = itemOpt.get();
        Image image = imageOpt.get();

        image.setImageTitle(imageTitle);
        imageRepository.save(image);

        item.getImages().add(image);
        itemRepository.save(item);

        return Optional.of(image);
    }
    @Transactional
    public Image uploadAndSave(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String original = file.getOriginalFilename();
        String baseName = (original == null ? "unnamed" : Paths.get(original).getFileName().toString());
        String safe = baseName.replaceAll("[^a-zA-Z0-9\\.\\-_]", "_");
        String unique = UUID.randomUUID() + "_" + safe;

        Path uploadDir = Paths.get(System.getProperty("user.dir"), "uploads", "images");
        Files.createDirectories(uploadDir);

        Path dest = uploadDir.resolve(unique).normalize();
        if (!dest.startsWith(uploadDir)) {
            throw new SecurityException("Invalid file path");
        }

        Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);

        Image img = new Image();
        img.setFileName(unique);
        img.setImageTitle(original);
        return imageRepository.save(img);
    }

    @Transactional(readOnly = true)
    public List<Image> findImagesByIds(Collection<Long> ids) {
        return (ids == null || ids.isEmpty())
                ? List.of()
                : imageRepository.findAllById(ids);
    }

    @Transactional
    public void deleteImageFile(Image image) {
        Path path = Paths.get(System.getProperty("user.dir"), "uploads", "images", image.getFileName());
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            System.err.println("Could not delete image file: " + path + " due to " + e.getMessage());
        }
    }

    @Transactional
    public void deleteImage(Image image) {
        deleteImageFile(image);

        for (Item item : new ArrayList<>(image.getItems())) {
            item.getImages().remove(image);
        }
        imageRepository.delete(image);
    }

    @Transactional
    public void deleteImageIfOrphaned(Image image) {
        if (image.getItems() == null || image.getItems().size() <= 1) {
            deleteImage(image);
        }
    }
}

package com.example.collection_manager.services;

import com.example.collection_manager.dtos.ImageUploadDTO;
import com.example.collection_manager.models.Image;
import com.example.collection_manager.models.Item;
import com.example.collection_manager.repositories.ImageRepository;
import com.example.collection_manager.repositories.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ImageService {

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private ItemRepository itemRepository;

    public Image saveImageMetadata(ImageUploadDTO dto) {
        Image image = new Image();
        image.setFileName(dto.fileName());
        image.setImageTitle(dto.imageTitle());
        return imageRepository.save(image);
    }

    public Optional<Image> saveAndAttachImageToItem(Long itemId, String fileName, String imageTitle) {
        Optional<Item> itemOpt = itemRepository.findById(itemId);
        if (itemOpt.isEmpty()) return Optional.empty();

        Item item = itemOpt.get();

        Image image = new Image();
        image.setFileName(fileName);
        image.setImageTitle(imageTitle);

        imageRepository.save(image);

        item.getImages().add(image);

        itemRepository.save(item);

        return Optional.of(image);
    }


}

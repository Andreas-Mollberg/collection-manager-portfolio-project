
package com.example.collection_manager.forms;

import org.springframework.web.multipart.MultipartFile;
import java.util.ArrayList;
import java.util.List;

public class CreateItemForm {

    private String itemName;
    private String description;
    private List<String> tagNames = new ArrayList<>();
    private List<Long> existingTagIds = new ArrayList<>();
    private List<MultipartFile> images = new ArrayList<>();

    public CreateItemForm() {
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getTagNames() {
        return tagNames;
    }

    public void setTagNames(List<String> tagNames) {
        this.tagNames = tagNames != null ? tagNames : new ArrayList<>();
    }

    public List<Long> getExistingTagIds() {
        return existingTagIds;
    }

    public void setExistingTagIds(List<Long> existingTagIds) {
        this.existingTagIds = existingTagIds != null ? existingTagIds : new ArrayList<>();
    }

    public List<MultipartFile> getImages() {
        return images;
    }

    public void setImages(List<MultipartFile> images) {
        this.images = images != null ? images : new ArrayList<>();
    }
}
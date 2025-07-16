package com.example.collection_manager.dtos;

import com.example.collection_manager.enums.Visibility;

import java.util.List;

public class CreateCollectionDTO {

    private String collectionTitle;
    private List<String> tags;
    private Visibility visibility;

    public String getCollectionTitle() {
        return collectionTitle;
    }

    public void setCollectionTitle(String collectionTitle) {
        this.collectionTitle = collectionTitle;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }
}

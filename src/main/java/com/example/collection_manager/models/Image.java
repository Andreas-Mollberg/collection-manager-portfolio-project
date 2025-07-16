package com.example.collection_manager.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.List;

@Entity
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToMany(mappedBy = "images")
    private List<Item> items;

    private String fileName;

    private String imageTitle;

    public Image() {
    }

    public Image(List<Item> items, String fileName, String imageTitle) {
        this.items = items;
        this.fileName = fileName;
        this.imageTitle = imageTitle;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String imageUrl) {
        this.fileName = imageUrl;
    }

    public String getImageTitle() {
        return imageTitle;
    }

    public void setImageTitle(String imageTitle) {
        this.imageTitle = imageTitle;
    }
}

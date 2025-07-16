package com.example.collection_manager.models;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String itemName;
    private String description;

    @ManyToMany
    @JoinTable(name = "item_tag", joinColumns = @JoinColumn(name = "item_id"), inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private List<Tag> tags;

    @ManyToMany
    @JoinTable(name = "item_image", joinColumns = @JoinColumn(name = "item_id"), inverseJoinColumns = @JoinColumn(name = "image_id"))
    private List<Image> images;

    @ManyToOne
    @JoinColumn(name = "collection_id")
    private Collection collection;

    public Item(String itemName, String description, List<Tag> tags, List<Image> images) {
        this.itemName = itemName;
        this.description = description;
        this.tags = tags;
        this.images = images;
    }

    public Item() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public List<Image> getImages() {
        return images;
    }

    public void setImages(List<Image> images) {
        this.images = images;
    }

    public Collection getCollection() {
        return collection;
    }

    public void setCollection(Collection collection) {
        this.collection = collection;
    }
}

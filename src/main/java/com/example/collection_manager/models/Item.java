package com.example.collection_manager.models;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String itemName;
    private String description;

    @ManyToOne
    @JoinColumn(name = "collection_id")
    private Collection collection;

    @ManyToMany
    @JoinTable(
            name = "item_tag",
            joinColumns = @JoinColumn(name = "item_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<Tag> tags = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "item_image",
            joinColumns = @JoinColumn(name = "item_id"),
            inverseJoinColumns = @JoinColumn(name = "image_id")
    )
    private List<Image> images = new ArrayList<>();

    public Item() {
    }

    public Item(String itemName, String description) {
        this.itemName = itemName;
        this.description = description;
    }

    public Item(String itemName, String description, List<Tag> tags, List<Image> images) {
        this.itemName = itemName;
        this.description = description;
        this.tags = tags != null ? new ArrayList<>(tags) : new ArrayList<>();
        this.images = images != null ? new ArrayList<>(images) : new ArrayList<>();
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

    public Collection getCollection() {
        return collection;
    }

    public void setCollection(Collection collection) {
        this.collection = collection;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags != null ? tags : new ArrayList<>();
    }

    public List<Image> getImages() {
        return images;
    }

    public void setImages(List<Image> images) {
        this.images = images != null ? images : new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return Objects.equals(id, item.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
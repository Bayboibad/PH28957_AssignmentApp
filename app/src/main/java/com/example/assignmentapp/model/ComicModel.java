package com.example.assignmentapp.model;

import java.io.Serializable;
import java.util.List;

public class ComicModel implements Serializable {
    private String id;
    private String name;
    private String author;
    private String coverImage;
    private List<String> contentImages;

    private String publicationYear;
    private String description;

    public ComicModel(String id, String name, String author, String coverImage, List<String> contentImages, String publicationYear, String description) {
        this.id = id;
        this.name = name;
        this.author = author;
        this.coverImage = coverImage;
        this.contentImages = contentImages;
        this.publicationYear = publicationYear;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public List<String> getContentImages() {
        return contentImages;
    }

    public void setContentImages(List<String> contentImages) {
        this.contentImages = contentImages;
    }

    public String getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(String publicationYear) {
        this.publicationYear = publicationYear;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

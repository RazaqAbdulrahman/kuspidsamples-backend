package com.kuspidsamples.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "samples", indexes = {
        @Index(name = "idx_sample_user", columnList = "user_id")
})
public class Sample extends BaseEntity {

    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "image_url")
    private String imageUrl; // Cloudinary URL

    @Column(name = "image_public_id")
    private String imagePublicId; // Cloudinary public_id for deletion

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Owner of the sample

    // Constructors
    public Sample() {}

    public Sample(String name, User user) {
        this.name = name;
        this.user = user;
    }

    public Sample(String name, String description, User user) {
        this.name = name;
        this.description = description;
        this.user = user;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImagePublicId() {
        return imagePublicId;
    }

    public void setImagePublicId(String imagePublicId) {
        this.imagePublicId = imagePublicId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "Sample{" +
                "id=" + getId() +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", user=" + (user != null ? user.getUsername() : null) +
                ", createdAt=" + getCreatedAt() +
                '}';
    }
}
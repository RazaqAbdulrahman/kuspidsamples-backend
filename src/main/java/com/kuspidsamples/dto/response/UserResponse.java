package com.kuspidsamples.dto.response;

import com.kuspidsamples.entity.Role;
import java.time.LocalDateTime;

public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String profileImageUrl;
    private Role role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public UserResponse() {
    }

    public UserResponse(Long id, String username, String email, String fullName,
                        String profileImageUrl, Role role, LocalDateTime createdAt,
                        LocalDateTime updatedAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.profileImageUrl = profileImageUrl;
        this.role = role;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
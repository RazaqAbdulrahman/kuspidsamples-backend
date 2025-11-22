package com.kuspidsamples.service;

import com.kuspidsamples.dto.response.SampleResponse;
import com.kuspidsamples.dto.response.UserResponse;
import com.kuspidsamples.entity.Sample;
import com.kuspidsamples.entity.User;
import com.kuspidsamples.exception.ResourceNotFoundException;
import com.kuspidsamples.exception.UnauthorizedException;
import com.kuspidsamples.repository.UserRepository;
import com.kuspidsamples.util.Constants;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CloudinaryService cloudinaryService;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       CloudinaryService cloudinaryService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.cloudinaryService = cloudinaryService;
    }

    /**
     * Get currently authenticated user
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("User not authenticated");
        }

        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(Constants.USER_NOT_FOUND));
    }

    /**
     * Get user by ID
     */
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(Constants.USER_NOT_FOUND));
    }

    /**
     * Get user by username
     */
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(Constants.USER_NOT_FOUND));
    }

    /**
     * Get current user profile
     */
    public UserResponse getCurrentUserProfile() {
        User user = getCurrentUser();
        return mapToUserResponse(user);
    }

    /**
     * Get user profile by username
     */
    public UserResponse getUserProfile(String username) {
        User user = getUserByUsername(username);
        return mapToUserResponse(user);
    }

    /**
     * Update user profile
     */
    @Transactional
    public UserResponse updateProfile(String fullName, MultipartFile profileImage) {
        User user = getCurrentUser();

        // Update full name
        if (fullName != null && !fullName.isBlank()) {
            user.setFullName(fullName);
        }

        // Update profile image if provided
        if (profileImage != null && !profileImage.isEmpty()) {
            // Delete old image if exists
            if (user.getProfileImagePublicId() != null) {
                cloudinaryService.deleteFile(user.getProfileImagePublicId());
            }

            // Upload new image
            Map<String, Object> uploadResult = cloudinaryService.uploadFile(
                    profileImage,
                    Constants.CLOUDINARY_FOLDER + "/profiles"
            );
            user.setProfileImageUrl(uploadResult.get("url").toString());
            user.setProfileImagePublicId(uploadResult.get("publicId").toString());
        }

        user = userRepository.save(user);
        return mapToUserResponse(user);
    }

    /**
     * Change password
     */
    @Transactional
    public void changePassword(String currentPassword, String newPassword) {
        User user = getCurrentUser();

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new UnauthorizedException("Current password is incorrect");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /**
     * Delete user account
     */
    @Transactional
    public void deleteAccount() {
        User user = getCurrentUser();

        // Delete profile image if exists
        if (user.getProfileImagePublicId() != null) {
            cloudinaryService.deleteFile(user.getProfileImagePublicId());
        }

        userRepository.delete(user);
    }

    /**
     * Map User entity to UserResponse DTO
     */
    private UserResponse mapToUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getProfileImageUrl(),
                user.getRole(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    /**
     * Map Sample entity to SampleResponse DTO
     */
    private SampleResponse mapToSampleResponse(Sample sample) {
        return new SampleResponse(
                sample.getId(),
                sample.getName(),
                sample.getDescription(),
                sample.getImageUrl(),
                sample.getUser().getId(),
                sample.getUser().getUsername(),
                sample.getCreatedAt(),
                sample.getUpdatedAt()
        );
    }
}
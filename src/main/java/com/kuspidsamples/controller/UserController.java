package com.kuspidsamples.controller;

import com.kuspidsamples.controller.AuthController.ApiResponse;
import com.kuspidsamples.dto.response.UserResponse;
import com.kuspidsamples.service.UserService;
import com.kuspidsamples.util.Constants;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(Constants.USER_BASE_PATH)
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Get current user's profile
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
        UserResponse user = userService.getCurrentUserProfile();
        return ResponseEntity.ok(new ApiResponse<>(true, "User profile retrieved", user));
    }

    /**
     * Get user profile by username
     */
    @GetMapping("/profile/{username}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserProfile(@PathVariable String username) {
        UserResponse user = userService.getUserProfile(username);
        return ResponseEntity.ok(new ApiResponse<>(true, "User found", user));
    }

    /**
     * Update current user's profile
     */
    @PatchMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) MultipartFile profileImage) {
        UserResponse user = userService.updateProfile(fullName, profileImage);
        return ResponseEntity.ok(new ApiResponse<>(true, Constants.PROFILE_UPDATED, user));
    }

    /**
     * Change password
     */
    @PostMapping("/me/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @RequestBody ChangePasswordRequest request) {
        userService.changePassword(request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok(new ApiResponse<>(true, Constants.PASSWORD_UPDATED, null));
    }

    /**
     * Delete current user's account
     */
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> deleteAccount() {
        userService.deleteAccount();
        return ResponseEntity.ok(new ApiResponse<>(true, "Account deleted successfully", null));
    }

    /**
     * Change password request DTO
     */
    public static class ChangePasswordRequest {
        private String currentPassword;
        private String newPassword;

        public ChangePasswordRequest() {}

        public String getCurrentPassword() { return currentPassword; }
        public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }
}
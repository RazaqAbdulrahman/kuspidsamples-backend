package com.kuspidsamples.service;

import com.kuspidsamples.dto.response.UserResponse;
import com.kuspidsamples.entity.Role;
import com.kuspidsamples.entity.User;
import com.kuspidsamples.exception.ResourceNotFoundException;
import com.kuspidsamples.exception.UnauthorizedException;
import com.kuspidsamples.repository.UserRepository;
import com.kuspidsamples.util.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CloudinaryService cloudinaryService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setFullName("Test User");
        testUser.setRole(Role.ROLE_USER);
        testUser.setProfileImageUrl("https://cloudinary.com/image.jpg");
        testUser.setProfileImagePublicId("profile_123");
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void getCurrentUser_WhenAuthenticated_ReturnsUser() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");
        SecurityContextHolder.setContext(securityContext);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        User result = userService.getCurrentUser();

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void getCurrentUser_WhenNotAuthenticated_ThrowsUnauthorizedException() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);

        // Act & Assert
        assertThrows(UnauthorizedException.class, () -> userService.getCurrentUser());
    }

    @Test
    void getCurrentUser_WhenUserNotFound_ThrowsUsernameNotFoundException() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");
        SecurityContextHolder.setContext(securityContext);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> userService.getCurrentUser());
    }

    @Test
    void getUserById_WhenUserExists_ReturnsUser() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        User result = userService.getUserById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
        verify(userRepository).findById(1L);
    }

    @Test
    void getUserById_WhenUserNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> userService.getUserById(1L)
        );
        assertEquals(Constants.USER_NOT_FOUND, exception.getMessage());
    }

    @Test
    void getUserByUsername_WhenUserExists_ReturnsUser() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        User result = userService.getUserByUsername("testuser");

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void getUserByUsername_WhenUserNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> userService.getUserByUsername("testuser"));
    }

    @Test
    void getCurrentUserProfile_ReturnsUserResponse() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");
        SecurityContextHolder.setContext(securityContext);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        UserResponse result = userService.getCurrentUserProfile();

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("Test User", result.getFullName());
        assertEquals(Role.ROLE_USER, result.getRole());
    }

    @Test
    void getUserProfile_WhenUsernameExists_ReturnsUserResponse() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        UserResponse result = userService.getUserProfile("testuser");

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    void updateProfile_WithFullNameOnly_UpdatesSuccessfully() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");
        SecurityContextHolder.setContext(securityContext);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        UserResponse result = userService.updateProfile("Updated Name", null);

        // Assert
        assertNotNull(result);
        verify(userRepository).save(any(User.class));
        verify(cloudinaryService, never()).uploadFile(any(), any());
    }

    @Test
    void updateProfile_WithProfileImage_UploadsAndUpdatesImage() {
        // Arrange
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");
        SecurityContextHolder.setContext(securityContext);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        Map<String, Object> uploadResult = new HashMap<>();
        uploadResult.put("url", "https://cloudinary.com/new-image.jpg");
        uploadResult.put("publicId", "profile_456");
        when(cloudinaryService.uploadFile(any(), anyString())).thenReturn(uploadResult);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        UserResponse result = userService.updateProfile("Updated Name", mockFile);

        // Assert
        assertNotNull(result);
        verify(cloudinaryService).deleteFile("profile_123");
        verify(cloudinaryService).uploadFile(mockFile, Constants.CLOUDINARY_FOLDER + "/profiles");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void changePassword_WithCorrectCurrentPassword_UpdatesPassword() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");
        SecurityContextHolder.setContext(securityContext);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("currentPassword", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        userService.changePassword("currentPassword", "newPassword");

        // Assert
        verify(passwordEncoder).matches("currentPassword", "encodedPassword");
        verify(passwordEncoder).encode("newPassword");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void changePassword_WithIncorrectCurrentPassword_ThrowsUnauthorizedException() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");
        SecurityContextHolder.setContext(securityContext);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        // Act & Assert
        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> userService.changePassword("wrongPassword", "newPassword")
        );
        assertEquals("Current password is incorrect", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteAccount_WithProfileImage_DeletesUserAndImage() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");
        SecurityContextHolder.setContext(securityContext);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        userService.deleteAccount();

        // Assert
        verify(cloudinaryService).deleteFile("profile_123");
        verify(userRepository).delete(testUser);
    }

    @Test
    void deleteAccount_WithoutProfileImage_DeletesUserOnly() {
        // Arrange
        testUser.setProfileImagePublicId(null);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");
        SecurityContextHolder.setContext(securityContext);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        userService.deleteAccount();

        // Assert
        verify(cloudinaryService, never()).deleteFile(any());
        verify(userRepository).delete(testUser);
    }
}
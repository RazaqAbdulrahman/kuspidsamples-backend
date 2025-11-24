package com.kuspidsamples.service;

import com.kuspidsamples.dto.request.SampleRequest;
import com.kuspidsamples.dto.response.SampleResponse;
import com.kuspidsamples.entity.Role;
import com.kuspidsamples.entity.Sample;
import com.kuspidsamples.entity.User;
import com.kuspidsamples.exception.ResourceNotFoundException;
import com.kuspidsamples.exception.UnauthorizedException;
import com.kuspidsamples.repository.SampleRepository;
import com.kuspidsamples.util.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SampleServiceTest {

    @Mock
    private SampleRepository sampleRepository;

    @Mock
    private UserService userService;

    @Mock
    private CloudinaryService cloudinaryService;

    @InjectMocks
    private SampleService sampleService;

    private User testUser;
    private User otherUser;
    private Sample testSample;
    private SampleRequest sampleRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setRole(Role.ROLE_USER);

        otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername("otheruser");
        otherUser.setEmail("other@example.com");
        otherUser.setRole(Role.ROLE_USER);

        testSample = new Sample();
        testSample.setId(1L);
        testSample.setName("Test Sample");
        testSample.setDescription("Test Description");
        testSample.setImageUrl("https://cloudinary.com/sample.jpg");
        testSample.setImagePublicId("sample_123");
        testSample.setUser(testUser);
        testSample.setCreatedAt(LocalDateTime.now());
        testSample.setUpdatedAt(LocalDateTime.now());

        sampleRequest = new SampleRequest();
        sampleRequest.setName("New Sample");
        sampleRequest.setDescription("New Description");
    }

    @Test
    void createSample_WithoutImage_CreatesSuccessfully() {
        // Arrange
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(sampleRepository.save(any(Sample.class))).thenReturn(testSample);

        // Act
        SampleResponse result = sampleService.createSample(sampleRequest, null);

        // Assert
        assertNotNull(result);
        assertEquals("Test Sample", result.getName());
        assertEquals(1L, result.getUserId());
        verify(cloudinaryService, never()).uploadFile(any(), any());
        verify(sampleRepository).save(any(Sample.class));
    }

    @Test
    void createSample_WithImage_UploadsAndCreatesSuccessfully() {
        // Arrange
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(userService.getCurrentUser()).thenReturn(testUser);

        Map<String, Object> uploadResult = new HashMap<>();
        uploadResult.put("url", "https://cloudinary.com/new-sample.jpg");
        uploadResult.put("publicId", "sample_456");
        when(cloudinaryService.uploadFile(mockFile, Constants.CLOUDINARY_FOLDER))
                .thenReturn(uploadResult);
        when(sampleRepository.save(any(Sample.class))).thenReturn(testSample);

        // Act
        SampleResponse result = sampleService.createSample(sampleRequest, mockFile);

        // Assert
        assertNotNull(result);
        verify(cloudinaryService).uploadFile(mockFile, Constants.CLOUDINARY_FOLDER);
        verify(sampleRepository).save(any(Sample.class));
    }

    @Test
    void createSample_WithEmptyImage_DoesNotUpload() {
        // Arrange
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(true);
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(sampleRepository.save(any(Sample.class))).thenReturn(testSample);

        // Act
        SampleResponse result = sampleService.createSample(sampleRequest, mockFile);

        // Assert
        assertNotNull(result);
        verify(cloudinaryService, never()).uploadFile(any(), any());
        verify(sampleRepository).save(any(Sample.class));
    }

    @Test
    void getSampleById_WhenExists_ReturnsSample() {
        // Arrange
        when(sampleRepository.findById(1L)).thenReturn(Optional.of(testSample));

        // Act
        SampleResponse result = sampleService.getSampleById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Sample", result.getName());
        assertEquals("testuser", result.getUsername());
        verify(sampleRepository).findById(1L);
    }

    @Test
    void getSampleById_WhenNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(sampleRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> sampleService.getSampleById(1L)
        );
        assertEquals(Constants.SAMPLE_NOT_FOUND, exception.getMessage());
    }

    @Test
    void getAllSamples_ReturnsPagedResults() {
        // Arrange
        List<Sample> samples = Arrays.asList(testSample);
        Page<Sample> samplePage = new PageImpl<>(samples);
        when(sampleRepository.findAll(any(Pageable.class))).thenReturn(samplePage);

        // Act
        Page<SampleResponse> result = sampleService.getAllSamples(0, 20);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Test Sample", result.getContent().get(0).getName());
        verify(sampleRepository).findAll(any(Pageable.class));
    }

    @Test
    void getCurrentUserSamples_ReturnsUserSamples() {
        // Arrange
        List<Sample> samples = Arrays.asList(testSample);
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(sampleRepository.findByUser(testUser)).thenReturn(samples);

        // Act
        List<SampleResponse> result = sampleService.getCurrentUserSamples();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Sample", result.get(0).getName());
        verify(sampleRepository).findByUser(testUser);
    }

    @Test
    void getSamplesByUserId_ReturnsPagedUserSamples() {
        // Arrange
        List<Sample> samples = Arrays.asList(testSample);
        Page<Sample> samplePage = new PageImpl<>(samples);
        when(userService.getUserById(1L)).thenReturn(testUser);
        when(sampleRepository.findByUser(eq(testUser), any(Pageable.class)))
                .thenReturn(samplePage);

        // Act
        Page<SampleResponse> result = sampleService.getSamplesByUserId(1L, 0, 20);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Test Sample", result.getContent().get(0).getName());
        verify(userService).getUserById(1L);
        verify(sampleRepository).findByUser(eq(testUser), any(Pageable.class));
    }

    @Test
    void updateSample_WithNameAndDescription_UpdatesSuccessfully() {
        // Arrange
        when(sampleRepository.findById(1L)).thenReturn(Optional.of(testSample));
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(sampleRepository.save(any(Sample.class))).thenReturn(testSample);

        SampleRequest updateRequest = new SampleRequest();
        updateRequest.setName("Updated Sample");
        updateRequest.setDescription("Updated Description");

        // Act
        SampleResponse result = sampleService.updateSample(1L, updateRequest, null);

        // Assert
        assertNotNull(result);
        verify(sampleRepository).save(any(Sample.class));
        verify(cloudinaryService, never()).uploadFile(any(), any());
    }

    @Test
    void updateSample_WithNewImage_DeletesOldAndUploadsNew() {
        // Arrange
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(sampleRepository.findById(1L)).thenReturn(Optional.of(testSample));
        when(userService.getCurrentUser()).thenReturn(testUser);

        Map<String, Object> uploadResult = new HashMap<>();
        uploadResult.put("url", "https://cloudinary.com/updated-sample.jpg");
        uploadResult.put("publicId", "sample_789");
        when(cloudinaryService.uploadFile(mockFile, Constants.CLOUDINARY_FOLDER))
                .thenReturn(uploadResult);
        when(sampleRepository.save(any(Sample.class))).thenReturn(testSample);

        // Act
        SampleResponse result = sampleService.updateSample(1L, sampleRequest, mockFile);

        // Assert
        assertNotNull(result);
        verify(cloudinaryService).deleteFile("sample_123");
        verify(cloudinaryService).uploadFile(mockFile, Constants.CLOUDINARY_FOLDER);
        verify(sampleRepository).save(any(Sample.class));
    }

    @Test
    void updateSample_WhenNotOwner_ThrowsUnauthorizedException() {
        // Arrange
        when(sampleRepository.findById(1L)).thenReturn(Optional.of(testSample));
        when(userService.getCurrentUser()).thenReturn(otherUser);

        // Act & Assert
        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> sampleService.updateSample(1L, sampleRequest, null)
        );
        assertEquals(Constants.UNAUTHORIZED_ACCESS, exception.getMessage());
        verify(sampleRepository, never()).save(any(Sample.class));
    }

    @Test
    void updateSample_WhenNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(sampleRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                ResourceNotFoundException.class,
                () -> sampleService.updateSample(1L, sampleRequest, null)
        );
    }

    @Test
    void deleteSample_WhenOwner_DeletesSuccessfully() {
        // Arrange
        when(sampleRepository.findById(1L)).thenReturn(Optional.of(testSample));
        when(userService.getCurrentUser()).thenReturn(testUser);

        // Act
        sampleService.deleteSample(1L);

        // Assert
        verify(cloudinaryService).deleteFile("sample_123");
        verify(sampleRepository).delete(testSample);
    }

    @Test
    void deleteSample_WithoutImage_DeletesSampleOnly() {
        // Arrange
        testSample.setImagePublicId(null);
        when(sampleRepository.findById(1L)).thenReturn(Optional.of(testSample));
        when(userService.getCurrentUser()).thenReturn(testUser);

        // Act
        sampleService.deleteSample(1L);

        // Assert
        verify(cloudinaryService, never()).deleteFile(any());
        verify(sampleRepository).delete(testSample);
    }

    @Test
    void deleteSample_WhenNotOwner_ThrowsUnauthorizedException() {
        // Arrange
        when(sampleRepository.findById(1L)).thenReturn(Optional.of(testSample));
        when(userService.getCurrentUser()).thenReturn(otherUser);

        // Act & Assert
        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> sampleService.deleteSample(1L)
        );
        assertEquals(Constants.UNAUTHORIZED_ACCESS, exception.getMessage());
        verify(sampleRepository, never()).delete(any(Sample.class));
    }

    @Test
    void deleteSample_WhenNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(sampleRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                ResourceNotFoundException.class,
                () -> sampleService.deleteSample(1L)
        );
    }
}
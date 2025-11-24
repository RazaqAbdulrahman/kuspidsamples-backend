package com.kuspidsamples.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.kuspidsamples.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CloudinaryServiceTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private CloudinaryService cloudinaryService;

    private Map<String, Object> mockUploadResult;

    @BeforeEach
    void setUp() {
        mockUploadResult = new HashMap<>();
        mockUploadResult.put("secure_url", "https://cloudinary.com/uploaded-image.jpg");
        mockUploadResult.put("public_id", "folder/test-uuid");
        mockUploadResult.put("resource_type", "image");
        mockUploadResult.put("format", "jpg");
    }

    @Test
    void uploadFile_WithValidFile_ReturnsUploadResult() throws IOException {
        // Arrange
        byte[] fileBytes = "test file content".getBytes();
        when(multipartFile.getBytes()).thenReturn(fileBytes);
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(mockUploadResult);

        // Act
        Map<String, Object> result = cloudinaryService.uploadFile(multipartFile, "test-folder");

        // Assert
        assertNotNull(result);
        assertEquals("https://cloudinary.com/uploaded-image.jpg", result.get("url"));
        assertEquals("folder/test-uuid", result.get("publicId"));
        verify(cloudinary).uploader();
        verify(uploader).upload(eq(fileBytes), anyMap());
    }

    @Test
    void uploadFile_WithIOException_ThrowsBadRequestException() throws IOException {
        // Arrange
        when(multipartFile.getBytes()).thenThrow(new IOException("File read error"));

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> cloudinaryService.uploadFile(multipartFile, "test-folder")
        );
        assertTrue(exception.getMessage().contains("Failed to upload file"));
        assertTrue(exception.getMessage().contains("File read error"));
    }

    @Test
    void uploadFile_WithCloudinaryError_ThrowsBadRequestException() throws IOException {
        // Arrange
        byte[] fileBytes = "test file content".getBytes();
        when(multipartFile.getBytes()).thenReturn(fileBytes);
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), anyMap()))
                .thenThrow(new IOException("Cloudinary upload failed"));

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> cloudinaryService.uploadFile(multipartFile, "test-folder")
        );
        assertTrue(exception.getMessage().contains("Failed to upload file"));
        assertTrue(exception.getMessage().contains("Cloudinary upload failed"));
    }

    @Test
    void uploadFile_WithDifferentFolder_UsesCorrectFolder() throws IOException {
        // Arrange
        byte[] fileBytes = "test file content".getBytes();
        String customFolder = "custom-folder";
        when(multipartFile.getBytes()).thenReturn(fileBytes);
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(mockUploadResult);

        // Act
        Map<String, Object> result = cloudinaryService.uploadFile(multipartFile, customFolder);

        // Assert
        assertNotNull(result);
        verify(uploader).upload(any(byte[].class), argThat(map ->
                map.containsKey("folder") && map.get("folder").equals(customFolder)
        ));
    }

    @Test
    void uploadFile_GeneratesUniquePublicId() throws IOException {
        // Arrange
        byte[] fileBytes = "test file content".getBytes();
        when(multipartFile.getBytes()).thenReturn(fileBytes);
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(mockUploadResult);

        // Act
        Map<String, Object> result1 = cloudinaryService.uploadFile(multipartFile, "test-folder");
        Map<String, Object> result2 = cloudinaryService.uploadFile(multipartFile, "test-folder");

        // Assert
        assertNotNull(result1);
        assertNotNull(result2);
        // Both uploads should have been called with public_id parameter
        verify(uploader, times(2)).upload(any(byte[].class), argThat(map ->
                map.containsKey("public_id")
        ));
    }

    @Test
    void uploadFile_SetsResourceTypeToAuto() throws IOException {
        // Arrange
        byte[] fileBytes = "test file content".getBytes();
        when(multipartFile.getBytes()).thenReturn(fileBytes);
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(mockUploadResult);

        // Act
        cloudinaryService.uploadFile(multipartFile, "test-folder");

        // Assert
        verify(uploader).upload(any(byte[].class), argThat(map ->
                map.containsKey("resource_type") && map.get("resource_type").equals("auto")
        ));
    }

    @Test
    void deleteFile_WithValidPublicId_DeletesSuccessfully() throws IOException {
        // Arrange
        String publicId = "folder/test-image-123";
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.destroy(eq(publicId), anyMap())).thenReturn(new HashMap<>());

        // Act
        cloudinaryService.deleteFile(publicId);

        // Assert
        verify(cloudinary).uploader();
        verify(uploader).destroy(eq(publicId), anyMap());
    }

    @Test
    void deleteFile_WithIOException_DoesNotThrowException() throws IOException {
        // Arrange
        String publicId = "folder/test-image-123";
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.destroy(eq(publicId), anyMap()))
                .thenThrow(new IOException("Deletion failed"));

        // Act & Assert - Should not throw exception
        assertDoesNotThrow(() -> cloudinaryService.deleteFile(publicId));
        verify(uploader).destroy(eq(publicId), anyMap());
    }

    @Test
    void deleteFile_WithNullPublicId_HandlesGracefully() throws IOException {
        // Arrange
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.destroy(isNull(), anyMap())).thenReturn(new HashMap<>());

        // Act & Assert
        assertDoesNotThrow(() -> cloudinaryService.deleteFile(null));
        verify(uploader).destroy(isNull(), anyMap());
    }

    @Test
    void deleteFile_WithEmptyPublicId_HandlesGracefully() throws IOException {
        // Arrange
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.destroy(eq(""), anyMap())).thenReturn(new HashMap<>());

        // Act & Assert
        assertDoesNotThrow(() -> cloudinaryService.deleteFile(""));
        verify(uploader).destroy(eq(""), anyMap());
    }

    @Test
    void uploadFile_ReturnsCorrectUrlAndPublicId() throws IOException {
        // Arrange
        byte[] fileBytes = "test file content".getBytes();
        Map<String, Object> uploadResult = new HashMap<>();
        uploadResult.put("secure_url", "https://res.cloudinary.com/demo/image/upload/sample.jpg");
        uploadResult.put("public_id", "samples/sample-123-456");

        when(multipartFile.getBytes()).thenReturn(fileBytes);
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(uploadResult);

        // Act
        Map<String, Object> result = cloudinaryService.uploadFile(multipartFile, "samples");

        // Assert
        assertEquals(2, result.size());
        assertEquals("https://res.cloudinary.com/demo/image/upload/sample.jpg", result.get("url"));
        assertEquals("samples/sample-123-456", result.get("publicId"));
    }

    @Test
    void uploadFile_WithLargeFile_UploadsSuccessfully() throws IOException {
        // Arrange
        byte[] largeFileBytes = new byte[1024 * 1024 * 5]; // 5 MB
        when(multipartFile.getBytes()).thenReturn(largeFileBytes);
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(mockUploadResult);

        // Act
        Map<String, Object> result = cloudinaryService.uploadFile(multipartFile, "test-folder");

        // Assert
        assertNotNull(result);
        verify(uploader).upload(eq(largeFileBytes), anyMap());
    }

    @Test
    void deleteFile_MultipleDeletes_AllExecute() throws IOException {
        // Arrange
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.destroy(anyString(), anyMap())).thenReturn(new HashMap<>());

        // Act
        cloudinaryService.deleteFile("file1");
        cloudinaryService.deleteFile("file2");
        cloudinaryService.deleteFile("file3");

        // Assert
        verify(uploader, times(3)).destroy(anyString(), anyMap());
        verify(uploader).destroy(eq("file1"), anyMap());
        verify(uploader).destroy(eq("file2"), anyMap());
        verify(uploader).destroy(eq("file3"), anyMap());
    }
}
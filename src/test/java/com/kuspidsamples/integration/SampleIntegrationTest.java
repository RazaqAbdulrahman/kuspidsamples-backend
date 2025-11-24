package com.kuspidsamples.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kuspidsamples.dto.request.RegisterRequest;
import com.kuspidsamples.dto.response.AuthResponse;
import com.kuspidsamples.repository.SampleRepository;
import com.kuspidsamples.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SampleIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SampleRepository sampleRepository;

    private String accessToken;

    @BeforeEach
    void setUp() throws Exception {
        sampleRepository.deleteAll();
        userRepository.deleteAll();

        // Register and login to get access token
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        AuthResponse authResponse = objectMapper.readValue(
                objectMapper.readTree(responseBody).get("data").toString(),
                AuthResponse.class
        );

        accessToken = authResponse.getAccessToken();
    }

    @Test
    void createSample_WithValidData_ReturnsCreated() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "image",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        // Act & Assert
        mockMvc.perform(multipart("/api/samples")
                        .file(file)
                        .param("name", "Test Sample")
                        .param("description", "Test Description")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Test Sample"))
                .andExpect(jsonPath("$.data.description").value("Test Description"))
                .andExpect(jsonPath("$.data.username").value("testuser"));
    }

    @Test
    void createSample_WithoutAuthentication_ReturnsUnauthorized() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "image",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        // Act & Assert
        mockMvc.perform(multipart("/api/samples")
                        .file(file)
                        .param("name", "Test Sample")
                        .param("description", "Test Description"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createSample_WithInvalidName_ReturnsBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(multipart("/api/samples")
                        .param("name", "AB") // Too short
                        .param("description", "Test Description")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllSamples_ReturnsPagedResults() throws Exception {
        // Arrange - Create a sample first
        mockMvc.perform(multipart("/api/samples")
                        .param("name", "Test Sample")
                        .param("description", "Test Description")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isCreated());

        // Act & Assert
        mockMvc.perform(get("/api/samples")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void getSampleById_WhenExists_ReturnsSample() throws Exception {
        // Arrange - Create a sample
        MvcResult createResult = mockMvc.perform(multipart("/api/samples")
                        .param("name", "Test Sample")
                        .param("description", "Test Description")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andReturn();

        String responseBody = createResult.getResponse().getContentAsString();
        Long sampleId = objectMapper.readTree(responseBody).get("data").get("id").asLong();

        // Act & Assert
        mockMvc.perform(get("/api/samples/" + sampleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(sampleId))
                .andExpect(jsonPath("$.data.name").value("Test Sample"));
    }

    @Test
    void getSampleById_WhenNotExists_ReturnsNotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/samples/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCurrentUserSamples_ReturnsUserSamples() throws Exception {
        // Arrange - Create samples
        mockMvc.perform(multipart("/api/samples")
                        .param("name", "Sample 1")
                        .param("description", "Description 1")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isCreated());

        mockMvc.perform(multipart("/api/samples")
                        .param("name", "Sample 2")
                        .param("description", "Description 2")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isCreated());

        // Act & Assert
        mockMvc.perform(get("/api/samples/my-samples")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void updateSample_WhenOwner_UpdatesSuccessfully() throws Exception {
        // Arrange - Create a sample
        MvcResult createResult = mockMvc.perform(multipart("/api/samples")
                        .param("name", "Original Name")
                        .param("description", "Original Description")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andReturn();

        String responseBody = createResult.getResponse().getContentAsString();
        Long sampleId = objectMapper.readTree(responseBody).get("data").get("id").asLong();

        // Act & Assert
        mockMvc.perform(multipart("/api/samples/" + sampleId)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .param("name", "Updated Name")
                        .param("description", "Updated Description")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Updated Name"))
                .andExpect(jsonPath("$.data.description").value("Updated Description"));
    }

    @Test
    void deleteSample_WhenOwner_DeletesSuccessfully() throws Exception {
        // Arrange - Create a sample
        MvcResult createResult = mockMvc.perform(multipart("/api/samples")
                        .param("name", "Test Sample")
                        .param("description", "Test Description")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andReturn();

        String responseBody = createResult.getResponse().getContentAsString();
        Long sampleId = objectMapper.readTree(responseBody).get("data").get("id").asLong();

        // Act & Assert
        mockMvc.perform(delete("/api/samples/" + sampleId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Sample deleted successfully"));

        // Verify sample was deleted
        mockMvc.perform(get("/api/samples/" + sampleId))
                .andExpect(status().isNotFound());
    }
}
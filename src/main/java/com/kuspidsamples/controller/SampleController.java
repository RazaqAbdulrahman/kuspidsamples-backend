package com.kuspidsamples.controller;

import com.kuspidsamples.controller.AuthController.ApiResponse;
import com.kuspidsamples.dto.request.SampleRequest;
import com.kuspidsamples.dto.response.SampleResponse;
import com.kuspidsamples.service.SampleService;
import com.kuspidsamples.util.Constants;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping(Constants.SAMPLE_BASE_PATH)
public class SampleController {

    private final SampleService sampleService;

    public SampleController(SampleService sampleService) {
        this.sampleService = sampleService;
    }

    /**
     * Create a new sample
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<SampleResponse>> createSample(
            @Valid @ModelAttribute SampleRequest request,
            @RequestParam(required = false) MultipartFile image) {
        SampleResponse sample = sampleService.createSample(request, image);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Sample created successfully", sample));
    }

    /**
     * Get sample by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SampleResponse>> getSampleById(@PathVariable Long id) {
        SampleResponse sample = sampleService.getSampleById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Sample found", sample));
    }

    /**
     * Get all samples with pagination
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<SampleResponse>>> getAllSamples(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<SampleResponse> samples = sampleService.getAllSamples(page, size);
        return ResponseEntity.ok(new ApiResponse<>(true, "Samples retrieved", samples));
    }

    /**
     * Get current user's samples
     */
    @GetMapping("/my-samples")
    public ResponseEntity<ApiResponse<List<SampleResponse>>> getCurrentUserSamples() {
        List<SampleResponse> samples = sampleService.getCurrentUserSamples();
        return ResponseEntity.ok(new ApiResponse<>(true, "Your samples retrieved", samples));
    }

    /**
     * Get samples by user ID
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<Page<SampleResponse>>> getSamplesByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<SampleResponse> samples = sampleService.getSamplesByUserId(userId, page, size);
        return ResponseEntity.ok(new ApiResponse<>(true, "User samples retrieved", samples));
    }

    /**
     * Update sample
     */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<SampleResponse>> updateSample(
            @PathVariable Long id,
            @Valid @ModelAttribute SampleRequest request,
            @RequestParam(required = false) MultipartFile image) {
        SampleResponse sample = sampleService.updateSample(id, request, image);
        return ResponseEntity.ok(new ApiResponse<>(true, "Sample updated successfully", sample));
    }

    /**
     * Delete sample
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSample(@PathVariable Long id) {
        sampleService.deleteSample(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Sample deleted successfully", null));
    }
}
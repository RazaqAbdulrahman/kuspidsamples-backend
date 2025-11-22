package com.kuspidsamples.service;

import com.kuspidsamples.dto.request.SampleRequest;
import com.kuspidsamples.dto.response.SampleResponse;
import com.kuspidsamples.entity.Sample;
import com.kuspidsamples.entity.User;
import com.kuspidsamples.exception.ResourceNotFoundException;
import com.kuspidsamples.exception.UnauthorizedException;
import com.kuspidsamples.repository.SampleRepository;
import com.kuspidsamples.util.Constants;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SampleService {

    private final SampleRepository sampleRepository;
    private final UserService userService;
    private final CloudinaryService cloudinaryService;

    public SampleService(SampleRepository sampleRepository,
                         UserService userService,
                         CloudinaryService cloudinaryService) {
        this.sampleRepository = sampleRepository;
        this.userService = userService;
        this.cloudinaryService = cloudinaryService;
    }

    /**
     * Create a new sample
     */
    @Transactional
    public SampleResponse createSample(SampleRequest request, MultipartFile image) {
        User currentUser = userService.getCurrentUser();

        Sample sample = new Sample();
        sample.setName(request.getName());
        sample.setDescription(request.getDescription());
        sample.setUser(currentUser);

        // Upload image if provided
        if (image != null && !image.isEmpty()) {
            Map<String, Object> uploadResult = cloudinaryService.uploadFile(image, Constants.CLOUDINARY_FOLDER);
            sample.setImageUrl(uploadResult.get("url").toString());
            sample.setImagePublicId(uploadResult.get("publicId").toString());
        }

        sample = sampleRepository.save(sample);
        return mapToSampleResponse(sample);
    }

    /**
     * Get sample by ID
     */
    public SampleResponse getSampleById(Long id) {
        Sample sample = sampleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(Constants.SAMPLE_NOT_FOUND));
        return mapToSampleResponse(sample);
    }

    /**
     * Get all samples with pagination
     */
    public Page<SampleResponse> getAllSamples(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return sampleRepository.findAll(pageable)
                .map(this::mapToSampleResponse);
    }

    /**
     * Get current user's samples
     */
    public List<SampleResponse> getCurrentUserSamples() {
        User currentUser = userService.getCurrentUser();
        return sampleRepository.findByUser(currentUser).stream()
                .map(this::mapToSampleResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get samples by user ID
     */
    public Page<SampleResponse> getSamplesByUserId(Long userId, int page, int size) {
        User user = userService.getUserById(userId);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return sampleRepository.findByUser(user, pageable)
                .map(this::mapToSampleResponse);
    }

    /**
     * Update sample
     */
    @Transactional
    public SampleResponse updateSample(Long id, SampleRequest request, MultipartFile image) {
        Sample sample = sampleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(Constants.SAMPLE_NOT_FOUND));

        User currentUser = userService.getCurrentUser();

        // Check if user owns the sample
        if (!sample.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException(Constants.UNAUTHORIZED_ACCESS);
        }

        // Update fields
        if (request.getName() != null && !request.getName().isBlank()) {
            sample.setName(request.getName());
        }

        if (request.getDescription() != null) {
            sample.setDescription(request.getDescription());
        }

        // Update image if provided
        if (image != null && !image.isEmpty()) {
            // Delete old image if exists
            if (sample.getImagePublicId() != null) {
                cloudinaryService.deleteFile(sample.getImagePublicId());
            }

            // Upload new image
            Map<String, Object> uploadResult = cloudinaryService.uploadFile(image, Constants.CLOUDINARY_FOLDER);
            sample.setImageUrl(uploadResult.get("url").toString());
            sample.setImagePublicId(uploadResult.get("publicId").toString());
        }

        sample = sampleRepository.save(sample);
        return mapToSampleResponse(sample);
    }

    /**
     * Delete sample
     */
    @Transactional
    public void deleteSample(Long id) {
        Sample sample = sampleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(Constants.SAMPLE_NOT_FOUND));

        User currentUser = userService.getCurrentUser();

        // Check if user owns the sample
        if (!sample.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException(Constants.UNAUTHORIZED_ACCESS);
        }

        // Delete image from Cloudinary if exists
        if (sample.getImagePublicId() != null) {
            cloudinaryService.deleteFile(sample.getImagePublicId());
        }

        sampleRepository.delete(sample);
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
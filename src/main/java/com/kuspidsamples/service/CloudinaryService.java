package com.kuspidsamples.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.kuspidsamples.exception.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    /**
     * Upload file to Cloudinary
     */
    public Map<String, Object> uploadFile(MultipartFile file, String folder) {
        try {
            // Generate unique filename
            String publicId = folder + "/" + UUID.randomUUID().toString();

            // Upload to Cloudinary
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "public_id", publicId,
                            "folder", folder,
                            "resource_type", "auto"
                    )
            );

            // Return URL and public_id
            return Map.of(
                    "url", uploadResult.get("secure_url").toString(),
                    "publicId", uploadResult.get("public_id").toString()
            );

        } catch (IOException e) {
            throw new BadRequestException("Failed to upload file: " + e.getMessage());
        }
    }

    /**
     * Delete file from Cloudinary
     */
    public void deleteFile(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException e) {
            // Log error but don't throw exception
            System.err.println("Failed to delete file from Cloudinary: " + e.getMessage());
        }
    }
}
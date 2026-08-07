package com.vocabverse.shadowing.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.vocabverse.common.constant.ErrorCode;
import com.vocabverse.common.exception.BusinessException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CloudinaryVideoStorageService {

    private static final String STORAGE_PROVIDER = "CLOUDINARY";
    private static final int CHUNK_SIZE = 6_000_000;

    @Value("${storage.cloudinary.cloud-name:}")
    private String cloudName;

    @Value("${storage.cloudinary.api-key:}")
    private String apiKey;

    @Value("${storage.cloudinary.api-secret:}")
    private String apiSecret;

    @Value("${storage.cloudinary.upload-folder:vocabverse/shadowing}")
    private String uploadFolder;

    public CloudinaryUploadResult uploadAudio(File audioFile, String filename) {
        assertConfigured();
        try {
            Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
                    "cloud_name", cloudName,
                    "api_key", apiKey,
                    "api_secret", apiSecret
            ));

            String publicId = UUID.randomUUID().toString();
            Map<?, ?> result = cloudinary.uploader().upload(audioFile, ObjectUtils.asMap(
                    "resource_type", "raw",
                    "folder", uploadFolder,
                    "public_id", publicId
            ));

            String secureUrl = asString(result.get("secure_url"));
            String returnedPublicId = asString(result.get("public_id"));
            if (!StringUtils.hasText(secureUrl) || !StringUtils.hasText(returnedPublicId)) {
                throw new BusinessException(ErrorCode.VIDEO_PROCESSING_FAILED, "Cloudinary audio upload response is invalid");
            }

            return new CloudinaryUploadResult(
                    returnedPublicId,
                    secureUrl,
                    null,
                    STORAGE_PROVIDER,
                    null
            );
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessException(ErrorCode.VIDEO_PROCESSING_FAILED, "Failed to upload audio to Cloudinary", exception);
        }
    }

    public CloudinaryUploadResult uploadMp4(MultipartFile file) {
        assertConfigured();
        Path tempFile = null;
        try {
            tempFile = Files.createTempFile("shadowing-", ".mp4");
            file.transferTo(tempFile);

            Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
                    "cloud_name", cloudName,
                    "api_key", apiKey,
                    "api_secret", apiSecret
            ));

            String publicId = UUID.randomUUID().toString();
            Map<?, ?> result = cloudinary.uploader().uploadLarge(tempFile.toFile(), ObjectUtils.asMap(
                    "resource_type", "video",
                    "folder", uploadFolder,
                    "public_id", publicId,
                    "chunk_size", CHUNK_SIZE
            ));

            String secureUrl = asString(result.get("secure_url"));
            String returnedPublicId = asString(result.get("public_id"));
            if (!StringUtils.hasText(secureUrl) || !StringUtils.hasText(returnedPublicId)) {
                throw new BusinessException(ErrorCode.VIDEO_PROCESSING_FAILED, "Cloudinary upload response is invalid");
            }

            return new CloudinaryUploadResult(
                    returnedPublicId,
                    secureUrl,
                    buildThumbnailUrl(returnedPublicId),
                    STORAGE_PROVIDER,
                    formatDuration(result.get("duration"))
            );
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessException(ErrorCode.VIDEO_PROCESSING_FAILED, "Failed to upload video to Cloudinary", exception);
        } finally {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException ignored) {
                    // Temp cleanup failure should not hide the actual upload result.
                }
            }
        }
    }

    private void assertConfigured() {
        if (!StringUtils.hasText(cloudName) || !StringUtils.hasText(apiKey) || !StringUtils.hasText(apiSecret)) {
            throw new BusinessException(ErrorCode.VIDEO_PROCESSING_FAILED, "Cloudinary is not configured");
        }
    }

    private String buildThumbnailUrl(String publicId) {
        return "https://res.cloudinary.com/" + cloudName + "/video/upload/so_0/" + publicId + ".jpg";
    }

    private String formatDuration(Object value) {
        if (!(value instanceof Number number)) {
            return null;
        }
        long seconds = Math.round(number.doubleValue());
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long remainingSeconds = seconds % 60;
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, remainingSeconds);
        }
        return String.format("%02d:%02d", minutes, remainingSeconds);
    }

    private String asString(Object value) {
        return value == null ? null : value.toString();
    }

    public record CloudinaryUploadResult(
            String publicId,
            String videoUrl,
            String thumbnailUrl,
            String storageProvider,
            String duration
    ) {
    }
}

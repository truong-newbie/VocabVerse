package com.vocabverse.shadowing.service;

import com.vocabverse.common.constant.ErrorCode;
import com.vocabverse.common.exception.BusinessException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AudioExtractorService {

    private static final Logger log = LoggerFactory.getLogger(AudioExtractorService.class);

    @Value("${audio.extractor.ffmpeg-path:ffmpeg}")
    private String ffmpegPath;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    public Path downloadAndExtractAudio(String videoUrl) {
        Path videoFile = null;
        Path audioFile = null;
        try {
            log.info("Downloading video from: {}", videoUrl);
            videoFile = downloadFile(videoUrl, ".mp4");

            String audioFileName = videoFile.getFileName().toString().replaceFirst("\\.[^.]+$", "") + ".m4a";
            audioFile = videoFile.getParent().resolve(audioFileName);

            log.info("Extracting audio with ffmpeg to: {}", audioFile);
            extractAudio(videoFile, audioFile);

            if (!Files.exists(audioFile) || Files.size(audioFile) == 0) {
                throw new BusinessException(ErrorCode.VIDEO_PROCESSING_FAILED, "FFmpeg produced an empty audio file");
            }

            log.info("Audio extraction complete: {} ({} bytes)", audioFile, Files.size(audioFile));
            return audioFile;
        } catch (BusinessException exception) {
            cleanupQuietly(audioFile);
            throw exception;
        } catch (Exception exception) {
            cleanupQuietly(audioFile);
            throw new BusinessException(ErrorCode.VIDEO_PROCESSING_FAILED,
                    "Failed to download video or extract audio: " + exception.getMessage(), exception);
        } finally {
            cleanupQuietly(videoFile);
        }
    }

    private Path downloadFile(String url, String extension) {
        try {
            Path tempFile = Files.createTempFile("shadowing-audio-", extension);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofMinutes(10))
                    .GET()
                    .build();

            HttpResponse<Path> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofFile(tempFile));

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                Files.deleteIfExists(tempFile);
                throw new BusinessException(ErrorCode.VIDEO_PROCESSING_FAILED,
                        "Failed to download video file, HTTP " + response.statusCode());
            }

            return tempFile;
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessException(ErrorCode.VIDEO_PROCESSING_FAILED,
                    "Failed to download video from URL: " + exception.getMessage(), exception);
        }
    }

    private void extractAudio(Path inputFile, Path outputFile) {
        try {
            // Resolve actual ffmpeg executable — try configured path first, then fall back to PATH
            String resolvedFfmpeg = resolveFfmpegPath();
            log.info("Using ffmpeg at: {}", resolvedFfmpeg);

            ProcessBuilder pb = new ProcessBuilder(
                    resolvedFfmpeg,
                    "-y",
                    "-i", inputFile.toString(),
                    "-vn",
                    "-acodec", "aac",
                    "-b:a", "128k",
                    outputFile.toString()
            );
            pb.redirectErrorStream(true);

            Process process = pb.start();

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                String output = new String(process.getInputStream().readAllBytes());
                throw new BusinessException(ErrorCode.VIDEO_PROCESSING_FAILED,
                        "FFmpeg exited with code " + exitCode + ": " + output);
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.VIDEO_PROCESSING_FAILED,
                    "FFmpeg process was interrupted", exception);
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.VIDEO_PROCESSING_FAILED,
                    "FFmpeg not found or not executable at: " + ffmpegPath, exception);
        }
    }

    private void cleanupQuietly(Path file) {
        if (file != null) {
            try {
                Files.deleteIfExists(file);
            } catch (IOException ignored) {
            }
        }
    }

    private String resolveFfmpegPath() {
        if (ffmpegPath != null && !ffmpegPath.isBlank()) {
            Path configPath = Path.of(ffmpegPath);
            if (Files.isRegularFile(configPath)) {
                return configPath.toAbsolutePath().toString();
            }
            log.warn("Configured ffmpeg path not found or not a file: {}", ffmpegPath);
        }
        // Fallback: rely on PATH
        return "ffmpeg";
    }
}

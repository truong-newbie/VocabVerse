package com.vocabverse.shadowing.service;

import com.vocabverse.common.constant.ErrorCode;
import com.vocabverse.common.exception.BusinessException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class YouTubeDownloadService {

    private static final Logger log = LoggerFactory.getLogger(YouTubeDownloadService.class);
    private static final Duration PROCESS_TIMEOUT = Duration.ofMinutes(15);
    private static final Pattern DURATION_PATTERN = Pattern.compile("^(\\d+(?:\\.\\d+)?)$");

    @Value("${youtube.download.yt-dlp-path:yt-dlp}")
    private String ytDlpPath;

    @Value("${audio.extractor.ffmpeg-path:ffmpeg}")
    private String ffmpegPath;

    @Value("${youtube.download.output-template:vocabverse-youtube-audio}")
    private String outputTemplate;

    public record DownloadResult(
            Path audioFile,
            String title,
            String duration
    ) {
    }

    public DownloadResult downloadAudio(String youtubeUrl) {
        if (youtubeUrl == null || youtubeUrl.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "YouTube URL is required");
        }

        if (!isValidYouTubeUrl(youtubeUrl)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "Invalid YouTube URL");
        }

        Path tempDir = null;
        try {
            tempDir = Files.createTempDirectory("shadowing-youtube-");
            log.info("Downloading audio from YouTube: {} to temp dir: {}", youtubeUrl, tempDir.toAbsolutePath());

            // Use absolute path for -o so yt-dlp writes to the temp dir regardless of working directory
            String absOutputTemplate = tempDir.resolve(outputTemplate).toAbsolutePath().toString()
                    .replace("\\", "/");
            StringBuilder output = new StringBuilder();
            int exitCode = runYtDlp(youtubeUrl, absOutputTemplate, output);

            log.info("yt-dlp output: [{}], exitCode: {}", output, exitCode);
            log.info("Temp dir contents: {}", java.util.Arrays.toString(Files.list(tempDir).toArray()));
            log.info("Current working directory: {}",
                    java.nio.file.Paths.get("").toAbsolutePath());

            if (exitCode != 0) {
                throw new BusinessException(ErrorCode.VIDEO_PROCESSING_FAILED,
                        "yt-dlp exited with code " + exitCode + ": " + output);
            }

            Path audioFile = findAudioFile(tempDir);
            // Fallback: search current directory if not in temp dir
            if (audioFile == null) {
                Path cwd = java.nio.file.Paths.get("").toAbsolutePath();
                log.warn("Audio file not found in temp dir {}, searching cwd: {}", tempDir, cwd);
                audioFile = findAudioFile(cwd);
            }

            if (audioFile == null || Files.size(audioFile) == 0) {
                throw new BusinessException(ErrorCode.VIDEO_PROCESSING_FAILED,
                        "yt-dlp did not produce a valid audio file");
            }

            String title = extractTitleFromOutput(output.toString());
            String duration = extractDurationFromOutput(output.toString());
            log.info("YouTube download complete: {} ({} bytes)", audioFile, Files.size(audioFile));
            return new DownloadResult(audioFile, title, duration);
        } catch (BusinessException exception) {
            cleanupQuietly(tempDir);
            throw exception;
        } catch (Exception exception) {
            cleanupQuietly(tempDir);
            throw new BusinessException(ErrorCode.VIDEO_PROCESSING_FAILED,
                    "Failed to download from YouTube: " + exception.getMessage(), exception);
        }
    }

    private int runYtDlp(String youtubeUrl, String absOutputTemplate, StringBuilder output) {
        try {
            java.util.List<String> command = new java.util.ArrayList<>();
            for (String part : ytDlpPath.split("\\s+")) {
                command.add(part);
            }
            command.add("-f"); command.add("bestaudio[ext=m4a]/bestaudio/best");
            command.add("-o"); command.add(absOutputTemplate + ".%(ext)s");
            command.add("--no-playlist");
            command.add("--print"); command.add("%(title)s");
            command.add("--print"); command.add("%(duration_string)s");
            if (ffmpegPath != null && !ffmpegPath.isBlank() && !"ffmpeg".equals(ffmpegPath)) {
                command.add("--ffmpeg-location"); command.add(ffmpegPath);
            }
            command.add(youtubeUrl);

            log.info("yt-dlp command: {}", String.join(" ", command));

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            byte[] buf = new byte[8192];
            int len;
            while ((len = process.getInputStream().read(buf)) != -1) {
                output.append(new String(buf, 0, len));
            }

            boolean finished = process.waitFor(PROCESS_TIMEOUT.toSeconds(), java.util.concurrent.TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new BusinessException(ErrorCode.VIDEO_PROCESSING_FAILED,
                        "yt-dlp process timed out after " + PROCESS_TIMEOUT);
            }
            return process.exitValue();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.VIDEO_PROCESSING_FAILED,
                    "yt-dlp process was interrupted", exception);
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.VIDEO_PROCESSING_FAILED,
                    "yt-dlp not found or not executable at: " + ytDlpPath, exception);
        }
    }

    private boolean isValidYouTubeUrl(String url) {
        return url.contains("youtube.com/watch") ||
               url.contains("youtu.be/") ||
               url.contains("youtube.com/shorts/");
    }

    private Path findAudioFile(Path dir) {
        try {
            return Files.list(dir)
                    .filter(p -> {
                        String name = p.getFileName().toString().toLowerCase();
                        return name.endsWith(".m4a") || name.endsWith(".mp3") || name.endsWith(".aac")
                               || name.endsWith(".webm") || name.endsWith(".opus");
                    })
                    .findFirst()
                    .orElse(null);
        } catch (IOException exception) {
            return null;
        }
    }

    private String extractTitleFromOutput(String output) {
        String[] lines = output.trim().split("\\r?\\n");
        for (String line : lines) {
            line = line.trim();
            if (!line.isEmpty() && !line.matches("\\d+:\\d+.*") && !line.matches("\\d+\\.\\d+.*")) {
                return line;
            }
        }
        return null;
    }

    private String extractDurationFromOutput(String output) {
        String[] lines = output.trim().split("\\r?\\n");
        for (String line : lines) {
            line = line.trim();
            if (line.matches("\\d+:\\d+.*")) {
                return line;
            }
        }
        return null;
    }

    private void cleanupQuietly(Path dir) {
        if (dir != null) {
            try {
                Files.walk(dir)
                        .sorted((a, b) -> b.compareTo(a))
                        .forEach(p -> { try { Files.deleteIfExists(p); } catch (IOException ignored) {} });
            } catch (IOException ignored) {
            }
        }
    }
}

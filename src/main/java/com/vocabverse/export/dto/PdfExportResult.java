package com.vocabverse.export.dto;

public record PdfExportResult(
        String filename,
        byte[] content
) {
}

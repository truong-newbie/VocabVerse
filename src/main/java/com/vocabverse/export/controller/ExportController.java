package com.vocabverse.export.controller;

import com.vocabverse.export.dto.PdfExportResult;
import com.vocabverse.export.service.PdfExportService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/export")
public class ExportController {

    private final PdfExportService pdfExportService;

    @GetMapping("/collections/{collectionId}/pdf")
    public ResponseEntity<byte[]> exportCollectionPdf(@PathVariable UUID collectionId) {
        return pdfResponse(pdfExportService.exportCollection(collectionId));
    }

    @GetMapping("/vocabularies/pdf")
    public ResponseEntity<byte[]> exportAllVocabulariesPdf() {
        return pdfResponse(pdfExportService.exportAllVocabularies());
    }

    private ResponseEntity<byte[]> pdfResponse(PdfExportResult result) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(result.filename())
                                .build()
                                .toString()
                )
                .body(result.content());
    }
}

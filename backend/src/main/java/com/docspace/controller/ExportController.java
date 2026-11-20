package com.docspace.controller;

import com.docspace.exception.BadRequestException;
import com.docspace.security.CurrentUser;
import com.docspace.service.ExportService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/documents/{documentId}/export")
public class ExportController {

    private final ExportService exportService;

    public ExportController(ExportService exportService) {
        this.exportService = exportService;
    }

    @GetMapping
    public ResponseEntity<byte[]> export(@PathVariable UUID documentId, @RequestParam String format) {
        UUID callerId = CurrentUser.id();

        ExportService.ExportedDocument exported;
        MediaType mediaType;
        String extension;

        switch (format.toLowerCase()) {
            case "markdown", "md" -> {
                exported = exportService.toMarkdown(documentId, callerId);
                mediaType = MediaType.parseMediaType("text/markdown");
                extension = "md";
            }
            case "pdf" -> {
                exported = exportService.toPdf(documentId, callerId);
                mediaType = MediaType.APPLICATION_PDF;
                extension = "pdf";
            }
            default -> throw new BadRequestException("Unsupported export format: " + format);
        }

        String filename = sanitizeFilename(exported.title()) + "." + extension;

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(filename).build().toString())
                .body(exported.bytes());
    }

    private String sanitizeFilename(String title) {
        String cleaned = title.replaceAll("[^a-zA-Z0-9 _-]", "").trim();
        return cleaned.isEmpty() ? "document" : cleaned;
    }
}

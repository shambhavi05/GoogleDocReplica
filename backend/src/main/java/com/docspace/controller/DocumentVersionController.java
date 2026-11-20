package com.docspace.controller;

import com.docspace.dto.DocumentVersionDetailResponse;
import com.docspace.dto.DocumentVersionSummaryResponse;
import com.docspace.security.CurrentUser;
import com.docspace.service.DocumentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/documents/{documentId}/versions")
public class DocumentVersionController {

    private final DocumentService documentService;

    public DocumentVersionController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping
    public List<DocumentVersionSummaryResponse> list(@PathVariable UUID documentId) {
        return documentService.listVersions(documentId, CurrentUser.id());
    }

    @GetMapping("/{versionId}")
    public DocumentVersionDetailResponse get(@PathVariable UUID documentId, @PathVariable UUID versionId) {
        return documentService.getVersion(documentId, versionId, CurrentUser.id());
    }
}

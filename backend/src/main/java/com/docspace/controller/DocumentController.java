package com.docspace.controller;

import com.docspace.dto.CreateDocumentRequest;
import com.docspace.dto.DocumentResponse;
import com.docspace.dto.DocumentSummaryResponse;
import com.docspace.dto.UpdateDocumentRequest;
import com.docspace.security.CurrentUser;
import com.docspace.service.DocumentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping
    public ResponseEntity<DocumentResponse> create(@Valid @RequestBody CreateDocumentRequest request) {
        DocumentResponse created = documentService.create(CurrentUser.id(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public Map<String, List<DocumentSummaryResponse>> list() {
        UUID userId = CurrentUser.id();
        return Map.of(
                "owned", documentService.listOwned(userId),
                "shared", documentService.listShared(userId)
        );
    }

    @GetMapping("/{id}")
    public DocumentResponse get(@PathVariable UUID id) {
        return documentService.get(id, CurrentUser.id());
    }

    @PutMapping("/{id}")
    public DocumentResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateDocumentRequest request) {
        return documentService.update(id, CurrentUser.id(), request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        documentService.delete(id, CurrentUser.id());
        return ResponseEntity.noContent().build();
    }
}

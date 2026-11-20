package com.docspace.controller;

import com.docspace.dto.DocumentResponse;
import com.docspace.security.CurrentUser;
import com.docspace.service.FileImportService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
public class FileImportController {

    private final FileImportService fileImportService;

    public FileImportController(FileImportService fileImportService) {
        this.fileImportService = fileImportService;
    }

    @PostMapping("/import")
    public ResponseEntity<DocumentResponse> importFile(@RequestParam("file") MultipartFile file) {
        DocumentResponse created = fileImportService.importFile(CurrentUser.id(), file);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}

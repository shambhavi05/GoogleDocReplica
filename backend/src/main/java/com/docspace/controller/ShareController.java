package com.docspace.controller;

import com.docspace.dto.CreateShareRequest;
import com.docspace.dto.ShareResponse;
import com.docspace.security.CurrentUser;
import com.docspace.service.ShareService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/documents/{documentId}/shares")
public class ShareController {

    private final ShareService shareService;

    public ShareController(ShareService shareService) {
        this.shareService = shareService;
    }

    @PostMapping
    public ResponseEntity<ShareResponse> share(@PathVariable UUID documentId,
                                                @Valid @RequestBody CreateShareRequest request) {
        ShareResponse created = shareService.share(documentId, CurrentUser.id(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public List<ShareResponse> list(@PathVariable UUID documentId) {
        return shareService.list(documentId, CurrentUser.id());
    }

    @DeleteMapping("/{shareId}")
    public ResponseEntity<Void> revoke(@PathVariable UUID documentId, @PathVariable UUID shareId) {
        shareService.revoke(documentId, shareId, CurrentUser.id());
        return ResponseEntity.noContent().build();
    }
}

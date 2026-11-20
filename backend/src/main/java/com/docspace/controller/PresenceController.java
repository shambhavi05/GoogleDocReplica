package com.docspace.controller;

import com.docspace.dto.UserResponse;
import com.docspace.repository.UserRepository;
import com.docspace.security.CurrentUser;
import com.docspace.service.DocumentService;
import com.docspace.service.PresenceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/documents/{documentId}/presence")
public class PresenceController {

    private final PresenceService presenceService;
    private final DocumentService documentService;
    private final UserRepository userRepository;

    public PresenceController(PresenceService presenceService,
                               DocumentService documentService,
                               UserRepository userRepository) {
        this.presenceService = presenceService;
        this.documentService = documentService;
        this.userRepository = userRepository;
    }

    @PostMapping("/heartbeat")
    public ResponseEntity<Void> heartbeat(@PathVariable UUID documentId) {
        UUID userId = CurrentUser.id();
        documentService.resolveAccess(documentId, userId); // 404s if caller has no access
        presenceService.heartbeat(documentId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public List<UserResponse> activeUsers(@PathVariable UUID documentId) {
        UUID callerId = CurrentUser.id();
        documentService.resolveAccess(documentId, callerId);

        List<UUID> activeIds = presenceService.activeUserIds(documentId, callerId);
        return userRepository.findAllById(activeIds).stream()
                .map(UserResponse::from)
                .toList();
    }
}

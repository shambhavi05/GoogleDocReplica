package com.docspace.service;

import com.docspace.dto.CreateShareRequest;
import com.docspace.dto.ShareResponse;
import com.docspace.entity.Document;
import com.docspace.entity.DocumentShare;
import com.docspace.entity.User;
import com.docspace.exception.BadRequestException;
import com.docspace.exception.ForbiddenException;
import com.docspace.exception.NotFoundException;
import com.docspace.repository.DocumentShareRepository;
import com.docspace.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ShareService {

    private final DocumentShareRepository shareRepository;
    private final UserRepository userRepository;
    private final DocumentService documentService;

    public ShareService(DocumentShareRepository shareRepository,
                         UserRepository userRepository,
                         DocumentService documentService) {
        this.shareRepository = shareRepository;
        this.userRepository = userRepository;
        this.documentService = documentService;
    }

    @Transactional
    public ShareResponse share(UUID documentId, UUID ownerId, CreateShareRequest request) {
        DocumentAccess access = documentService.resolveAccess(documentId, ownerId);
        if (!access.canManage()) {
            throw new ForbiddenException("Only the owner can manage sharing for this document");
        }

        Document document = access.document();

        User target = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new BadRequestException("No user found with email " + request.email()));

        if (target.getId().equals(document.getOwner().getId())) {
            throw new BadRequestException("Cannot share a document with its own owner");
        }

        if (shareRepository.findByDocumentIdAndSharedWithId(documentId, target.getId()).isPresent()) {
            throw new BadRequestException("This document is already shared with " + request.email());
        }

        DocumentShare share = new DocumentShare();
        share.setDocument(document);
        share.setSharedWith(target);
        share.setPermission(request.permission());

        try {
            DocumentShare saved = shareRepository.save(share);
            return ShareResponse.from(saved);
        } catch (DataIntegrityViolationException ex) {
            throw new BadRequestException("This document is already shared with " + request.email());
        }
    }

    @Transactional(readOnly = true)
    public List<ShareResponse> list(UUID documentId, UUID callerId) {
        DocumentAccess access = documentService.resolveAccess(documentId, callerId);
        if (!access.canManage()) {
            throw new ForbiddenException("Only the owner can view collaborators for this document");
        }
        return shareRepository.findByDocumentIdOrderByCreatedAtAsc(documentId).stream()
                .map(ShareResponse::from)
                .toList();
    }

    @Transactional
    public void revoke(UUID documentId, UUID shareId, UUID ownerId) {
        DocumentAccess access = documentService.resolveAccess(documentId, ownerId);
        if (!access.canManage()) {
            throw new ForbiddenException("Only the owner can manage sharing for this document");
        }

        DocumentShare share = shareRepository.findByIdAndDocumentId(shareId, documentId)
                .orElseThrow(() -> new NotFoundException("Share not found"));

        shareRepository.delete(share);
    }
}

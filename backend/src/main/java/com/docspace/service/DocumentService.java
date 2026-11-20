package com.docspace.service;

import com.docspace.dto.CreateDocumentRequest;
import com.docspace.dto.DocumentResponse;
import com.docspace.dto.DocumentSummaryResponse;
import com.docspace.dto.UpdateDocumentRequest;
import com.docspace.dto.DocumentVersionDetailResponse;
import com.docspace.dto.DocumentVersionSummaryResponse;
import com.docspace.entity.Document;
import com.docspace.entity.DocumentShare;
import com.docspace.entity.DocumentVersion;
import com.docspace.entity.User;
import com.docspace.exception.BadRequestException;
import com.docspace.exception.ConflictException;
import com.docspace.exception.ForbiddenException;
import com.docspace.exception.NotFoundException;
import com.docspace.repository.DocumentRepository;
import com.docspace.repository.DocumentShareRepository;
import com.docspace.repository.DocumentVersionRepository;
import com.docspace.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentShareRepository shareRepository;
    private final UserRepository userRepository;
    private final DocumentVersionRepository versionRepository;
    private final ObjectMapper objectMapper;

    public DocumentService(DocumentRepository documentRepository,
                            DocumentShareRepository shareRepository,
                            UserRepository userRepository,
                            DocumentVersionRepository versionRepository,
                            ObjectMapper objectMapper) {
        this.documentRepository = documentRepository;
        this.shareRepository = shareRepository;
        this.userRepository = userRepository;
        this.versionRepository = versionRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Resolves the caller's effective access to a document. Centralizing this here
     * means every controller path (read, edit, rename, delete, share-management)
     * goes through the same owner/editor/viewer check instead of re-implementing it.
     */
    @Transactional(readOnly = true)
    public DocumentAccess resolveAccess(UUID documentId, UUID userId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("Document not found"));

        boolean isOwner = document.getOwner().getId().equals(userId);
        if (isOwner) {
            return new DocumentAccess(document, true, null);
        }

        var share = shareRepository.findByDocumentIdAndSharedWithId(documentId, userId);
        if (share.isEmpty()) {
            // Not the owner and no share record: treat as not-found, not forbidden,
            // so we never confirm to an unauthorized caller that the document exists.
            throw new NotFoundException("Document not found");
        }

        return new DocumentAccess(document, false, share.get().getPermission());
    }

    @Transactional
    public DocumentResponse create(UUID ownerId, CreateDocumentRequest request) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Document document = new Document();
        document.setTitle(request.title());
        document.setOwner(owner);
        document.setContent(blankDocumentJson());

        Document saved = documentRepository.save(document);
        return toResponse(saved, true, null);
    }

    @Transactional(readOnly = true)
    public List<DocumentSummaryResponse> listOwned(UUID ownerId) {
        return documentRepository.findByOwnerIdOrderByUpdatedAtDesc(ownerId).stream()
                .map(doc -> DocumentSummaryResponse.from(doc, "OWNER"))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DocumentSummaryResponse> listShared(UUID userId) {
        return documentRepository.findSharedWithUser(userId).stream()
                .map(doc -> {
                    com.docspace.entity.Permission permission = shareRepository
                            .findByDocumentIdAndSharedWithId(doc.getId(), userId)
                            .map(DocumentShare::getPermission)
                            .orElse(null);
                    return DocumentSummaryResponse.from(doc, permission == null ? null : permission.name());
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public DocumentResponse get(UUID documentId, UUID userId) {
        DocumentAccess access = resolveAccess(documentId, userId);
        return toResponse(access.document(), access.owner(), access.permission());
    }

    @Transactional
    public DocumentResponse update(UUID documentId, UUID userId, UpdateDocumentRequest request) {
        DocumentAccess access = resolveAccess(documentId, userId);
        if (!access.canEdit()) {
            throw new ForbiddenException("You do not have permission to edit this document");
        }

        Document document = access.document();

        if (!document.getVersion().equals(request.version())) {
            throw new ConflictException(
                    "Document was modified elsewhere since you last loaded it",
                    toResponse(document, access.owner(), access.permission())
            );
        }

        User saver = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        snapshotVersion(document, saver);

        if (request.title() != null && !request.title().isBlank()) {
            if (!access.owner()) {
                throw new ForbiddenException("Only the owner can rename this document");
            }
            document.setTitle(request.title());
        }

        if (request.content() != null) {
            document.setContent(request.content().toString());
        }

        try {
            Document saved = documentRepository.save(document);
            return toResponse(saved, access.owner(), access.permission());
        } catch (ObjectOptimisticLockingFailureException ex) {
            Document latest = documentRepository.findById(documentId)
                    .orElseThrow(() -> new NotFoundException("Document not found"));
            throw new ConflictException(
                    "Document was modified elsewhere since you last loaded it",
                    toResponse(latest, access.owner(), access.permission())
            );
        }
    }

    private void snapshotVersion(Document document, User saver) {
        DocumentVersion snapshot = new DocumentVersion();
        snapshot.setDocument(document);
        snapshot.setContent(document.getContent());
        snapshot.setTitle(document.getTitle());
        snapshot.setVersion(document.getVersion());
        snapshot.setSavedBy(saver);
        versionRepository.save(snapshot);
    }

    @Transactional(readOnly = true)
    public List<DocumentVersionSummaryResponse> listVersions(UUID documentId, UUID userId) {
        DocumentAccess access = resolveAccess(documentId, userId);
        if (!access.canRead()) {
            throw new ForbiddenException("You do not have access to this document's history");
        }
        return versionRepository.findByDocumentIdOrderByCreatedAtDesc(documentId).stream()
                .map(DocumentVersionSummaryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public DocumentVersionDetailResponse getVersion(UUID documentId, UUID versionId, UUID userId) {
        DocumentAccess access = resolveAccess(documentId, userId);
        if (!access.canRead()) {
            throw new ForbiddenException("You do not have access to this document's history");
        }
        DocumentVersion snapshot = versionRepository.findByIdAndDocumentId(versionId, documentId)
                .orElseThrow(() -> new NotFoundException("Version not found"));
        return DocumentVersionDetailResponse.from(snapshot, parseContent(snapshot.getContent()));
    }

    @Transactional
    public void delete(UUID documentId, UUID userId) {
        DocumentAccess access = resolveAccess(documentId, userId);
        if (!access.canManage()) {
            throw new ForbiddenException("Only the owner can delete this document");
        }
        documentRepository.delete(access.document());
    }

    @Transactional
    public DocumentResponse createFromImport(UUID ownerId, String title, JsonNode content) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (title == null || title.isBlank()) {
            throw new BadRequestException("A title could not be derived from the uploaded file");
        }

        Document document = new Document();
        document.setTitle(title.length() > 500 ? title.substring(0, 500) : title);
        document.setOwner(owner);
        document.setContent(content.toString());

        Document saved = documentRepository.save(document);
        return toResponse(saved, true, null);
    }

    private DocumentResponse toResponse(Document document, boolean isOwner, com.docspace.entity.Permission permission) {
        JsonNode content = parseContent(document.getContent());
        String role = com.docspace.dto.DocumentResponse.roleFor(isOwner, permission);
        return com.docspace.dto.DocumentResponse.from(document, content, role);
    }

    private JsonNode parseContent(String raw) {
        try {
            return objectMapper.readTree(raw);
        } catch (Exception ex) {
            return blankDocumentNode();
        }
    }

    private String blankDocumentJson() {
        return blankDocumentNode().toString();
    }

    private JsonNode blankDocumentNode() {
        var root = objectMapper.createObjectNode();
        root.put("type", "doc");
        var content = root.putArray("content");
        content.addObject().put("type", "paragraph");
        return root;
    }
}

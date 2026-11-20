package com.docspace.service;

import com.docspace.dto.CreateDocumentRequest;
import com.docspace.dto.DocumentResponse;
import com.docspace.dto.UpdateDocumentRequest;
import com.docspace.entity.Document;
import com.docspace.entity.DocumentShare;
import com.docspace.entity.Permission;
import com.docspace.entity.User;
import com.docspace.exception.ConflictException;
import com.docspace.exception.ForbiddenException;
import com.docspace.exception.NotFoundException;
import com.docspace.repository.DocumentRepository;
import com.docspace.repository.DocumentShareRepository;
import com.docspace.repository.DocumentVersionRepository;
import com.docspace.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private DocumentShareRepository shareRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DocumentVersionRepository versionRepository;

    @InjectMocks
    private DocumentService documentService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private User owner;
    private User editorUser;
    private User viewerUser;
    private User strangerUser;
    private Document document;

    @BeforeEach
    void setUp() {
        // Real ObjectMapper injected manually since @InjectMocks only wires mocks
        documentService = new DocumentService(documentRepository, shareRepository, userRepository, versionRepository, objectMapper);

        owner = buildUser("Alice", "alice@docspace.test");
        editorUser = buildUser("Bob", "bob@docspace.test");
        viewerUser = buildUser("Charlie", "charlie@docspace.test");
        strangerUser = buildUser("Dave", "dave@docspace.test");

        document = new Document();
        document.setId(UUID.randomUUID());
        document.setTitle("Product Roadmap");
        document.setContent("{\"type\":\"doc\",\"content\":[]}");
        document.setOwner(owner);
        document.setVersion(0);
    }

    private User buildUser(String name, String email) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setName(name);
        user.setEmail(email);
        return user;
    }

    private DocumentShare shareFor(User user, Permission permission) {
        DocumentShare share = new DocumentShare();
        share.setId(UUID.randomUUID());
        share.setDocument(document);
        share.setSharedWith(user);
        share.setPermission(permission);
        return share;
    }

    // 1. Document creation
    @Test
    void createsDocumentOwnedByCaller() {
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> {
            Document doc = invocation.getArgument(0);
            doc.setId(UUID.randomUUID());
            doc.setVersion(0);
            return doc;
        });

        DocumentResponse response = documentService.create(owner.getId(), new CreateDocumentRequest("New Doc"));

        assertThat(response.title()).isEqualTo("New Doc");
        assertThat(response.role()).isEqualTo("OWNER");
        verify(documentRepository).save(any(Document.class));
    }

    // 2. Document update (by owner)
    @Test
    void ownerCanUpdateContentAndTitle() {
        when(documentRepository.findById(document.getId())).thenReturn(Optional.of(document));
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(documentRepository.save(any(Document.class))).thenAnswer(inv -> inv.getArgument(0));

        ObjectNode newContent = objectMapper.createObjectNode().put("type", "doc");
        UpdateDocumentRequest request = new UpdateDocumentRequest("Renamed", newContent, 0);

        DocumentResponse response = documentService.update(document.getId(), owner.getId(), request);

        assertThat(response.title()).isEqualTo("Renamed");
        assertThat(response.role()).isEqualTo("OWNER");
    }

    // 3. Viewer cannot edit
    @Test
    void viewerCannotEditDocument() {
        when(documentRepository.findById(document.getId())).thenReturn(Optional.of(document));
        when(shareRepository.findByDocumentIdAndSharedWithId(document.getId(), viewerUser.getId()))
                .thenReturn(Optional.of(shareFor(viewerUser, Permission.VIEWER)));

        UpdateDocumentRequest request = new UpdateDocumentRequest(null, objectMapper.createObjectNode(), 0);

        assertThatThrownBy(() -> documentService.update(document.getId(), viewerUser.getId(), request))
                .isInstanceOf(ForbiddenException.class);

        verify(documentRepository, never()).save(any());
    }

    // 4. Editor can edit
    @Test
    void editorCanEditDocument() {
        when(documentRepository.findById(document.getId())).thenReturn(Optional.of(document));
        when(shareRepository.findByDocumentIdAndSharedWithId(document.getId(), editorUser.getId()))
                .thenReturn(Optional.of(shareFor(editorUser, Permission.EDITOR)));
        when(userRepository.findById(editorUser.getId())).thenReturn(Optional.of(editorUser));
        when(documentRepository.save(any(Document.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateDocumentRequest request = new UpdateDocumentRequest(null, objectMapper.createObjectNode(), 0);

        DocumentResponse response = documentService.update(document.getId(), editorUser.getId(), request);

        assertThat(response.role()).isEqualTo("EDITOR");
        verify(documentRepository).save(any(Document.class));
    }

    // 5. Non-owner cannot delete
    @Test
    void editorCannotDeleteDocument() {
        when(documentRepository.findById(document.getId())).thenReturn(Optional.of(document));
        when(shareRepository.findByDocumentIdAndSharedWithId(document.getId(), editorUser.getId()))
                .thenReturn(Optional.of(shareFor(editorUser, Permission.EDITOR)));

        assertThatThrownBy(() -> documentService.delete(document.getId(), editorUser.getId()))
                .isInstanceOf(ForbiddenException.class);

        verify(documentRepository, never()).delete(any());
    }

    @Test
    void ownerCanDeleteDocument() {
        when(documentRepository.findById(document.getId())).thenReturn(Optional.of(document));

        documentService.delete(document.getId(), owner.getId());

        verify(documentRepository).delete(document);
    }

    // IDOR: a user with no relationship to the document gets 404, not 403
    @Test
    void strangerGetsNotFoundInsteadOfForbidden() {
        when(documentRepository.findById(document.getId())).thenReturn(Optional.of(document));
        when(shareRepository.findByDocumentIdAndSharedWithId(document.getId(), strangerUser.getId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentService.get(document.getId(), strangerUser.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    // Optimistic concurrency: stale version is rejected with a conflict
    @Test
    void updateWithStaleVersionThrowsConflict() {
        document.setVersion(2);
        when(documentRepository.findById(document.getId())).thenReturn(Optional.of(document));

        UpdateDocumentRequest request = new UpdateDocumentRequest(null, objectMapper.createObjectNode(), 0);

        assertThatThrownBy(() -> documentService.update(document.getId(), owner.getId(), request))
                .isInstanceOf(ConflictException.class);

        verify(documentRepository, never()).save(any());
    }
}

package com.docspace.service;

import com.docspace.dto.CreateShareRequest;
import com.docspace.entity.Document;
import com.docspace.entity.DocumentShare;
import com.docspace.entity.Permission;
import com.docspace.entity.User;
import com.docspace.exception.ForbiddenException;
import com.docspace.repository.DocumentShareRepository;
import com.docspace.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShareServiceTest {

    @Mock
    private DocumentShareRepository shareRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DocumentService documentService;

    private ShareService shareService;

    private User owner;
    private User editorUser;
    private User targetUser;
    private Document document;

    @BeforeEach
    void setUp() {
        shareService = new ShareService(shareRepository, userRepository, documentService);

        owner = buildUser("Alice", "alice@docspace.test");
        editorUser = buildUser("Bob", "bob@docspace.test");
        targetUser = buildUser("Charlie", "charlie@docspace.test");

        document = new Document();
        document.setId(UUID.randomUUID());
        document.setTitle("Product Roadmap");
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

    // 6. Non-owner cannot manage sharing
    @Test
    void editorCannotShareDocument() {
        DocumentAccess editorAccess = new DocumentAccess(document, false, Permission.EDITOR);
        when(documentService.resolveAccess(document.getId(), editorUser.getId())).thenReturn(editorAccess);

        CreateShareRequest request = new CreateShareRequest("charlie@docspace.test", Permission.VIEWER);

        assertThatThrownBy(() -> shareService.share(document.getId(), editorUser.getId(), request))
                .isInstanceOf(ForbiddenException.class);

        verify(shareRepository, never()).save(any());
    }

    @Test
    void editorCannotRevokeShares() {
        DocumentAccess editorAccess = new DocumentAccess(document, false, Permission.EDITOR);
        when(documentService.resolveAccess(document.getId(), editorUser.getId())).thenReturn(editorAccess);

        assertThatThrownBy(() -> shareService.revoke(document.getId(), UUID.randomUUID(), editorUser.getId()))
                .isInstanceOf(ForbiddenException.class);

        verify(shareRepository, never()).delete(any());
    }

    @Test
    void ownerCanShareDocumentWithNewUser() {
        DocumentAccess ownerAccess = new DocumentAccess(document, true, null);
        when(documentService.resolveAccess(document.getId(), owner.getId())).thenReturn(ownerAccess);
        when(userRepository.findByEmailIgnoreCase("charlie@docspace.test")).thenReturn(Optional.of(targetUser));
        when(shareRepository.findByDocumentIdAndSharedWithId(document.getId(), targetUser.getId()))
                .thenReturn(Optional.empty());
        when(shareRepository.save(any(DocumentShare.class))).thenAnswer(inv -> {
            DocumentShare share = inv.getArgument(0);
            share.setId(UUID.randomUUID());
            return share;
        });

        CreateShareRequest request = new CreateShareRequest("charlie@docspace.test", Permission.VIEWER);
        var response = shareService.share(document.getId(), owner.getId(), request);

        assertThat(response.user().email()).isEqualTo("charlie@docspace.test");
        assertThat(response.permission()).isEqualTo("VIEWER");
    }

    // Duplicate sharing must be rejected
    @Test
    void duplicateShareIsRejected() {
        DocumentAccess ownerAccess = new DocumentAccess(document, true, null);
        when(documentService.resolveAccess(document.getId(), owner.getId())).thenReturn(ownerAccess);
        when(userRepository.findByEmailIgnoreCase("charlie@docspace.test")).thenReturn(Optional.of(targetUser));

        DocumentShare existing = new DocumentShare();
        existing.setDocument(document);
        existing.setSharedWith(targetUser);
        existing.setPermission(Permission.VIEWER);
        when(shareRepository.findByDocumentIdAndSharedWithId(document.getId(), targetUser.getId()))
                .thenReturn(Optional.of(existing));

        CreateShareRequest request = new CreateShareRequest("charlie@docspace.test", Permission.EDITOR);

        assertThatThrownBy(() -> shareService.share(document.getId(), owner.getId(), request))
                .isInstanceOf(com.docspace.exception.BadRequestException.class);

        verify(shareRepository, never()).save(any());
    }
}

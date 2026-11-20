package com.docspace.service;

import com.docspace.dto.CreateCommentRequest;
import com.docspace.entity.Document;
import com.docspace.entity.DocumentComment;
import com.docspace.entity.Permission;
import com.docspace.entity.User;
import com.docspace.exception.ForbiddenException;
import com.docspace.repository.DocumentCommentRepository;
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
class CommentServiceTest {

    @Mock
    private DocumentCommentRepository commentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DocumentService documentService;

    private CommentService commentService;

    private User owner;
    private User editorUser;
    private User viewerUser;
    private Document document;

    @BeforeEach
    void setUp() {
        commentService = new CommentService(commentRepository, userRepository, documentService);

        owner = buildUser("Shambhavi");
        editorUser = buildUser("Bob");
        viewerUser = buildUser("Charlie");

        document = new Document();
        document.setId(UUID.randomUUID());
        document.setTitle("Product Roadmap");
        document.setOwner(owner);
        document.setVersion(0);
    }

    private User buildUser(String name) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setName(name);
        user.setEmail(name.toLowerCase() + "@example.com");
        return user;
    }

    @Test
    void viewerCanAddComment() {
        DocumentAccess viewerAccess = new DocumentAccess(document, false, Permission.VIEWER);
        when(documentService.resolveAccess(document.getId(), viewerUser.getId())).thenReturn(viewerAccess);
        when(userRepository.findById(viewerUser.getId())).thenReturn(Optional.of(viewerUser));
        when(commentRepository.save(any(DocumentComment.class))).thenAnswer(inv -> {
            DocumentComment c = inv.getArgument(0);
            c.setId(UUID.randomUUID());
            return c;
        });

        var response = commentService.create(document.getId(), viewerUser.getId(), new CreateCommentRequest("Nice draft!"));

        assertThat(response.body()).isEqualTo("Nice draft!");
        assertThat(response.author().id()).isEqualTo(viewerUser.getId());
    }

    @Test
    void authorCanResolveTheirOwnComment() {
        DocumentAccess editorAccess = new DocumentAccess(document, false, Permission.EDITOR);
        when(documentService.resolveAccess(document.getId(), editorUser.getId())).thenReturn(editorAccess);

        DocumentComment comment = new DocumentComment();
        comment.setId(UUID.randomUUID());
        comment.setDocument(document);
        comment.setAuthor(editorUser);
        comment.setBody("Left a note");
        when(commentRepository.findByIdAndDocumentId(comment.getId(), document.getId()))
                .thenReturn(Optional.of(comment));
        when(commentRepository.save(any(DocumentComment.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = commentService.resolve(document.getId(), comment.getId(), editorUser.getId());

        assertThat(response.resolved()).isTrue();
    }

    @Test
    void nonAuthorNonOwnerCannotResolveSomeoneElsesComment() {
        DocumentAccess viewerAccess = new DocumentAccess(document, false, Permission.VIEWER);
        when(documentService.resolveAccess(document.getId(), viewerUser.getId())).thenReturn(viewerAccess);

        DocumentComment comment = new DocumentComment();
        comment.setId(UUID.randomUUID());
        comment.setDocument(document);
        comment.setAuthor(editorUser);
        comment.setBody("Bob's comment");
        when(commentRepository.findByIdAndDocumentId(comment.getId(), document.getId()))
                .thenReturn(Optional.of(comment));

        assertThatThrownBy(() -> commentService.resolve(document.getId(), comment.getId(), viewerUser.getId()))
                .isInstanceOf(ForbiddenException.class);

        verify(commentRepository, never()).save(any());
    }

    @Test
    void ownerCanDeleteAnyCommentOnTheirDocument() {
        DocumentAccess ownerAccess = new DocumentAccess(document, true, null);
        when(documentService.resolveAccess(document.getId(), owner.getId())).thenReturn(ownerAccess);

        DocumentComment comment = new DocumentComment();
        comment.setId(UUID.randomUUID());
        comment.setDocument(document);
        comment.setAuthor(editorUser);
        comment.setBody("Bob's comment");
        when(commentRepository.findByIdAndDocumentId(comment.getId(), document.getId()))
                .thenReturn(Optional.of(comment));

        commentService.delete(document.getId(), comment.getId(), owner.getId());

        verify(commentRepository).delete(comment);
    }
}

package com.docspace.service;

import com.docspace.dto.CommentResponse;
import com.docspace.dto.CreateCommentRequest;
import com.docspace.entity.Document;
import com.docspace.entity.DocumentComment;
import com.docspace.entity.User;
import com.docspace.exception.ForbiddenException;
import com.docspace.exception.NotFoundException;
import com.docspace.repository.DocumentCommentRepository;
import com.docspace.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CommentService {

    private final DocumentCommentRepository commentRepository;
    private final UserRepository userRepository;
    private final DocumentService documentService;

    public CommentService(DocumentCommentRepository commentRepository,
                           UserRepository userRepository,
                           DocumentService documentService) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.documentService = documentService;
    }

    @Transactional
    public CommentResponse create(UUID documentId, UUID authorId, CreateCommentRequest request) {
        DocumentAccess access = documentService.resolveAccess(documentId, authorId);
        if (!access.canRead()) {
            throw new ForbiddenException("You do not have access to comment on this document");
        }

        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        DocumentComment comment = new DocumentComment();
        comment.setDocument(access.document());
        comment.setAuthor(author);
        comment.setBody(request.body());

        return CommentResponse.from(commentRepository.save(comment));
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> list(UUID documentId, UUID callerId) {
        DocumentAccess access = documentService.resolveAccess(documentId, callerId);
        if (!access.canRead()) {
            throw new ForbiddenException("You do not have access to view comments on this document");
        }

        return commentRepository.findByDocumentIdOrderByCreatedAtAsc(documentId).stream()
                .map(CommentResponse::from)
                .toList();
    }

    @Transactional
    public CommentResponse resolve(UUID documentId, UUID commentId, UUID callerId) {
        DocumentAccess access = documentService.resolveAccess(documentId, callerId);
        if (!access.canRead()) {
            throw new ForbiddenException("You do not have access to this document");
        }

        DocumentComment comment = findCommentOrThrow(documentId, commentId);
        requireAuthorOrOwner(comment, access, callerId, "resolve");

        comment.setResolved(true);
        return CommentResponse.from(commentRepository.save(comment));
    }

    @Transactional
    public void delete(UUID documentId, UUID commentId, UUID callerId) {
        DocumentAccess access = documentService.resolveAccess(documentId, callerId);
        if (!access.canRead()) {
            throw new ForbiddenException("You do not have access to this document");
        }

        DocumentComment comment = findCommentOrThrow(documentId, commentId);
        requireAuthorOrOwner(comment, access, callerId, "delete");

        commentRepository.delete(comment);
    }

    private DocumentComment findCommentOrThrow(UUID documentId, UUID commentId) {
        return commentRepository.findByIdAndDocumentId(commentId, documentId)
                .orElseThrow(() -> new NotFoundException("Comment not found"));
    }

    private void requireAuthorOrOwner(DocumentComment comment, DocumentAccess access, UUID callerId, String action) {
        boolean isAuthor = comment.getAuthor().getId().equals(callerId);
        if (!isAuthor && !access.owner()) {
            throw new ForbiddenException("Only the comment's author or the document owner can " + action + " it");
        }
    }
}

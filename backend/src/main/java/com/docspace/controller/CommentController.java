package com.docspace.controller;

import com.docspace.dto.CommentResponse;
import com.docspace.dto.CreateCommentRequest;
import com.docspace.security.CurrentUser;
import com.docspace.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/documents/{documentId}/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public ResponseEntity<CommentResponse> create(@PathVariable UUID documentId,
                                                   @Valid @RequestBody CreateCommentRequest request) {
        CommentResponse created = commentService.create(documentId, CurrentUser.id(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public List<CommentResponse> list(@PathVariable UUID documentId) {
        return commentService.list(documentId, CurrentUser.id());
    }

    @PatchMapping("/{commentId}/resolve")
    public CommentResponse resolve(@PathVariable UUID documentId, @PathVariable UUID commentId) {
        return commentService.resolve(documentId, commentId, CurrentUser.id());
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> delete(@PathVariable UUID documentId, @PathVariable UUID commentId) {
        commentService.delete(documentId, commentId, CurrentUser.id());
        return ResponseEntity.noContent().build();
    }
}

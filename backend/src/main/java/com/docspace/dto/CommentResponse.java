package com.docspace.dto;

import com.docspace.entity.DocumentComment;

import java.time.Instant;
import java.util.UUID;

public record CommentResponse(
        UUID id,
        UserResponse author,
        String body,
        boolean resolved,
        Instant createdAt
) {
    public static CommentResponse from(DocumentComment comment) {
        return new CommentResponse(
                comment.getId(),
                UserResponse.from(comment.getAuthor()),
                comment.getBody(),
                comment.isResolved(),
                comment.getCreatedAt()
        );
    }
}

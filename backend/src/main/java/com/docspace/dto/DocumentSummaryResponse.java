package com.docspace.dto;

import com.docspace.entity.Document;

import java.time.Instant;
import java.util.UUID;

public record DocumentSummaryResponse(
        UUID id,
        String title,
        UserResponse owner,
        String role,
        Instant createdAt,
        Instant updatedAt
) {
    public static DocumentSummaryResponse from(Document document, String role) {
        return new DocumentSummaryResponse(
                document.getId(),
                document.getTitle(),
                UserResponse.from(document.getOwner()),
                role,
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
    }
}

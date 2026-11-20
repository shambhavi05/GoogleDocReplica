package com.docspace.dto;

import com.docspace.entity.DocumentVersion;

import java.time.Instant;
import java.util.UUID;

public record DocumentVersionSummaryResponse(
        UUID id,
        String title,
        Integer version,
        UserResponse savedBy,
        Instant createdAt
) {
    public static DocumentVersionSummaryResponse from(DocumentVersion snapshot) {
        return new DocumentVersionSummaryResponse(
                snapshot.getId(),
                snapshot.getTitle(),
                snapshot.getVersion(),
                UserResponse.from(snapshot.getSavedBy()),
                snapshot.getCreatedAt()
        );
    }
}

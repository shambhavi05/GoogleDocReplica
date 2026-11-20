package com.docspace.dto;

import com.docspace.entity.DocumentVersion;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.UUID;

public record DocumentVersionDetailResponse(
        UUID id,
        String title,
        JsonNode content,
        Integer version,
        UserResponse savedBy,
        Instant createdAt
) {
    public static DocumentVersionDetailResponse from(DocumentVersion snapshot, JsonNode content) {
        return new DocumentVersionDetailResponse(
                snapshot.getId(),
                snapshot.getTitle(),
                content,
                snapshot.getVersion(),
                UserResponse.from(snapshot.getSavedBy()),
                snapshot.getCreatedAt()
        );
    }
}

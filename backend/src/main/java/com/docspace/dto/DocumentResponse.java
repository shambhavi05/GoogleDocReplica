package com.docspace.dto;

import com.docspace.entity.Document;
import com.docspace.entity.Permission;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.UUID;

public record DocumentResponse(
        UUID id,
        String title,
        JsonNode content,
        UserResponse owner,
        Integer version,
        String role,
        Instant createdAt,
        Instant updatedAt
) {
    public static DocumentResponse from(Document document, JsonNode content, String role) {
        return new DocumentResponse(
                document.getId(),
                document.getTitle(),
                content,
                UserResponse.from(document.getOwner()),
                document.getVersion(),
                role,
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
    }

    public static String roleFor(boolean isOwner, Permission permission) {
        if (isOwner) return "OWNER";
        return permission == null ? null : permission.name();
    }
}

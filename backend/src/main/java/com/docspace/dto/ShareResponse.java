package com.docspace.dto;

import com.docspace.entity.DocumentShare;

import java.time.Instant;
import java.util.UUID;

public record ShareResponse(
        UUID id,
        UserResponse user,
        String permission,
        Instant createdAt
) {
    public static ShareResponse from(DocumentShare share) {
        return new ShareResponse(
                share.getId(),
                UserResponse.from(share.getSharedWith()),
                share.getPermission().name(),
                share.getCreatedAt()
        );
    }
}

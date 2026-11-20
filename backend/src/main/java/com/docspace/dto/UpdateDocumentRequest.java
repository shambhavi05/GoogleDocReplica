package com.docspace.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateDocumentRequest(
        @Size(max = 500, message = "Title must be at most 500 characters")
        String title,

        JsonNode content,

        @NotNull(message = "version is required to detect conflicting edits")
        Integer version
) {
}

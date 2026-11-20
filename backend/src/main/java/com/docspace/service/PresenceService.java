package com.docspace.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory, process-local presence tracking: who has a document open right now.
 * Intentionally not persisted — presence has no meaning across a restart, and a
 * lightweight heartbeat/poll model is enough for "who else is viewing this" without
 * building WebSocket push or live cursor sync.
 */
@Service
public class PresenceService {

    private static final long STALE_AFTER_SECONDS = 15;

    private final Map<UUID, Map<UUID, Instant>> presenceByDocument = new ConcurrentHashMap<>();

    public void heartbeat(UUID documentId, UUID userId) {
        presenceByDocument
                .computeIfAbsent(documentId, id -> new ConcurrentHashMap<>())
                .put(userId, Instant.now());
    }

    public List<UUID> activeUserIds(UUID documentId, UUID excludingUserId) {
        Map<UUID, Instant> seen = presenceByDocument.get(documentId);
        if (seen == null) {
            return List.of();
        }

        Instant cutoff = Instant.now().minusSeconds(STALE_AFTER_SECONDS);
        seen.values().removeIf(lastSeen -> lastSeen.isBefore(cutoff));

        return seen.keySet().stream()
                .filter(userId -> !userId.equals(excludingUserId))
                .toList();
    }
}

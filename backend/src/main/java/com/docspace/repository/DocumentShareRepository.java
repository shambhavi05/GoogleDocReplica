package com.docspace.repository;

import com.docspace.entity.DocumentShare;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentShareRepository extends JpaRepository<DocumentShare, UUID> {

    List<DocumentShare> findByDocumentIdOrderByCreatedAtAsc(UUID documentId);

    Optional<DocumentShare> findByDocumentIdAndSharedWithId(UUID documentId, UUID sharedWithId);

    Optional<DocumentShare> findByIdAndDocumentId(UUID id, UUID documentId);
}

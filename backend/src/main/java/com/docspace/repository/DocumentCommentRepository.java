package com.docspace.repository;

import com.docspace.entity.DocumentComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentCommentRepository extends JpaRepository<DocumentComment, UUID> {

    List<DocumentComment> findByDocumentIdOrderByCreatedAtAsc(UUID documentId);

    Optional<DocumentComment> findByIdAndDocumentId(UUID id, UUID documentId);
}

package com.docspace.repository;

import com.docspace.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<Document, UUID> {

    List<Document> findByOwnerIdOrderByUpdatedAtDesc(UUID ownerId);

    @org.springframework.data.jpa.repository.Query(
            "select d from Document d join DocumentShare ds on ds.document = d " +
                    "where ds.sharedWith.id = :userId order by d.updatedAt desc"
    )
    List<Document> findSharedWithUser(UUID userId);
}

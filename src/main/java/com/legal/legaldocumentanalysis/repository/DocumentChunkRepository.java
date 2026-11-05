
package com.legal.legaldocumentanalysis.repository;

import com.legal.legaldocumentanalysis.model.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentChunkRepository
        extends JpaRepository<DocumentChunk, Long> {

    List<DocumentChunk> findByDocumentIdOrderByChunkIndexAsc(
            Long documentId);

    void deleteByDocumentId(Long documentId);
}
